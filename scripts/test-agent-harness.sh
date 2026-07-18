#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "$0")/.." && pwd)"
fixture_root="$(mktemp -d)"
trap 'rm -rf "$fixture_root"' EXIT

prompt_context="$fixture_root/agent-context.md"
prompt_metadata="$fixture_root/agent-context.metadata"
prompt_outputs="$fixture_root/prompt.outputs"
baseline_shadow="$fixture_root/baseline-shadow.json"
cat > "$baseline_shadow" <<'JSON'
[
  {
    "seed": 17,
    "requestedSteps": 5,
    "completedSteps": 5,
    "status": "completed",
    "initial": {"cycle": 10, "total": 8, "nutrients": 12, "nutrientBuffer": 20, "counts": {}},
    "final": {"cycle": 15, "total": 9, "nutrients": 10, "nutrientBuffer": 20, "counts": {"MOSS": 1, "ROOT_NETWORK": 1, "SPORE": 1, "FERN": 1, "FUNGUS": 1, "BEETLE": 1, "HARE": 1, "FOX": 2}},
    "minimumTotal": 8,
    "maximumTotal": 9,
    "minimumCounts": {},
    "maximumCounts": {}
  }
]
JSON
(
  cd "$repository_root"
  rg() {
    echo "Prompt construction must not depend on ripgrep." >&2
    return 127
  }
  export -f rg
  PATH=/usr/bin:/bin \
    AGENT_CONTEXT_METADATA_FILE="$prompt_metadata" \
    AGENT_CONTEXT_RECENT_JOURNAL_LIMIT=1 \
    AGENT_BASELINE_SHADOW_FILE="$baseline_shadow" \
    GARDEN_OUTCOME_HISTORY_LIMIT=1 \
    scripts/build-agent-context.sh "$prompt_context"
  PATH=/usr/bin:/bin GITHUB_OUTPUT="$prompt_outputs" \
    scripts/write-output-file.sh text "$prompt_context"
)
grep -Eq '^text<<agent_output_[0-9]+_[0-9]+_[0-9]+$' "$prompt_outputs"
grep -Fq '# AI Live Garden Compact Agent Context' "$prompt_outputs"
grep -Fq '## Baseline Shadow Simulation' "$prompt_outputs"
grep -Fq '| 17 | 5 | completed | 10 | 20 | 9 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 2 | 9 |' "$prompt_outputs"
grep -Fq 'scripts/evaluate-shadow-candidate.sh target/agent-baseline-shadow.json' "$prompt_outputs"
grep -Fq 'AGENT_CONTEXT_BYTES=' "$prompt_metadata"
grep -Fq "AGENT_CONTEXT_BASELINE_SHADOW_FILE=${baseline_shadow}" "$prompt_metadata"
prompt_delimiter="$(sed -n '1s/^text<<//p' "$prompt_outputs")"
[[ "$(tail -n 1 "$prompt_outputs")" == "$prompt_delimiter" ]]

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
