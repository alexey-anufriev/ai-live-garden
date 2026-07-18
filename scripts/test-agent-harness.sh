#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "$0")/.." && pwd)"
fixture_root="$(mktemp -d)"
trap 'rm -rf "$fixture_root"' EXIT

mkdir -p "$fixture_root/scripts" "$fixture_root/agent/plans" "$fixture_root/artifacts"
for script in find-active-agent-plan.sh agent-substantive-changes.sh validate-agent-handoff.sh \
  extract-agent-handoff.sh inspect-agent-gemini-output.sh validate-agent-worktree.sh; do
  cp "$repository_root/scripts/$script" "$fixture_root/scripts/$script"
done
chmod +x "$fixture_root/scripts/"*.sh

cat > "$fixture_root/agent/plans/2026-07-08.md" <<'PLAN'
# Daily Garden Direction - 2026-07-08

### A. Direction A
### B. Direction B
### C. Direction C
### D. Direction D
PLAN

cd "$fixture_root"
git init -q

handoff() {
  local direction="$1"
  local metric="$2"
  local goal="$3"
  local delta="$4"
  local output="$5"
  jq -n --arg direction "$direction" --arg metric "$metric" --arg goal "$goal" --argjson delta "$delta" '{
    title:"Harness fixture", task:"Exercise the autonomous contract.", why:"Regression coverage.",
    summary:"Fixture.", observations:"Fixture.", next:"Fixture.", expectedGardenEffect:"Fixture effect.",
    pmDirection:$direction,
    evidence:{bottleneck:"fixture",currentState:"fixture",verification:"fixture"},
    evaluation:{metric:$metric,goal:$goal,requiredDelta:$delta},
    codeMap:[],requests:[],state:{immediateDirections:[],constraints:[]}
  }' > "$output"
}

[[ "$(AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/find-active-agent-plan.sh)" == "agent/plans/2026-07-08.md" ]]
[[ -z "$(AGENT_PM_REFERENCE_DATE=2026-07-09 scripts/find-active-agent-plan.sh)" ]]

handoff A population.BEETLE increase 1 handoff-active.json
AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/validate-agent-handoff.sh handoff-active.json >/dev/null

handoff none population.BEETLE increase 1 handoff-stale.json
AGENT_PM_REFERENCE_DATE=2026-07-09 scripts/validate-agent-handoff.sh handoff-stale.json >/dev/null

handoff none tests pass 0 handoff-repair.json
AGENT_PM_REFERENCE_DATE=2026-07-08 AGENT_BASELINE_TEST_OUTCOME=failure \
  scripts/validate-agent-handoff.sh handoff-repair.json >/dev/null

if AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/validate-agent-handoff.sh handoff-stale.json >/dev/null 2>&1; then
  echo "Active plan incorrectly accepted pmDirection=none." >&2
  exit 1
fi

jq -n --rawfile response handoff-active.json \
  '{response:("AGENT_RUN_JSON_START\n" + $response + "AGENT_RUN_JSON_END")}' > artifacts/stdout.log
GITHUB_OUTPUT="$fixture_root/inspect.outputs" AGENT_PM_REFERENCE_DATE=2026-07-08 \
  scripts/inspect-agent-gemini-output.sh artifacts >/dev/null
grep -Fxq 'valid_handoff=true' inspect.outputs
grep -Fxq 'substantive_change=false' inspect.outputs
grep -Fxq 'retry_required=true' inspect.outputs
grep -Fxq 'noop_reason=handoff-without-substantive-change' inspect.outputs
rm -rf artifacts

if VALIDATE_AGENT_WORKTREE_SCOPE=changed scripts/validate-agent-worktree.sh >/dev/null 2>&1; then
  echo "Generated-only fixture incorrectly passed substantive-change policy." >&2
  exit 1
fi

mkdir -p src/main/java/example
echo 'package example;' > src/main/java/example/Change.java
scripts/agent-substantive-changes.sh | grep -Fxq 'src/main/java/example/Change.java'
VALIDATE_AGENT_WORKTREE_SCOPE=changed scripts/validate-agent-worktree.sh >/dev/null

echo "Agent harness regression tests passed."
