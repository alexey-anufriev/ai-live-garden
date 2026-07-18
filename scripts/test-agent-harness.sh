#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "$0")/.." && pwd)"
fixture_root="$(mktemp -d)"
trap 'rm -rf "$fixture_root"' EXIT

grep -Fq 'id: defer_shadow_rejection' "$repository_root/.github/workflows/evolve.yml"
grep -Fq 'scripts/defer-shadow-rejection.sh' "$repository_root/.github/workflows/evolve.yml"
grep -Fq 'id: commit_shadow_feedback' "$repository_root/.github/workflows/evolve.yml"
if grep -Fq 'build-shadow-retry-prompt-output.sh' "$repository_root/.github/workflows/evolve.yml"; then
  echo "The workflow reintroduced a same-run shadow corrective loop." >&2
  exit 1
fi

prompt_context="$fixture_root/agent-context.md"
prompt_metadata="$fixture_root/agent-context.metadata"
prompt_outputs="$fixture_root/prompt.outputs"
baseline_shadow="$fixture_root/baseline-shadow.json"
shadow_feedback="$fixture_root/shadow-feedback.md"
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
cat > "$shadow_feedback" <<'FEEDBACK'
# Deferred Shadow Evaluation Feedback

Previous candidate observed delta: 0.
FEEDBACK
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
    AGENT_SHADOW_FEEDBACK_FILE="$shadow_feedback" \
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
grep -Fq '## Previous Rejected Shadow Hypothesis' "$prompt_outputs"
grep -Fq 'Previous candidate observed delta: 0.' "$prompt_outputs"
grep -Fq 'AGENT_CONTEXT_BYTES=' "$prompt_metadata"
grep -Fq "AGENT_CONTEXT_BASELINE_SHADOW_FILE=${baseline_shadow}" "$prompt_metadata"
grep -Fq "AGENT_CONTEXT_SHADOW_FEEDBACK_FILE=${shadow_feedback}" "$prompt_metadata"
prompt_delimiter="$(sed -n '1s/^text<<//p' "$prompt_outputs")"
[[ "$(tail -n 1 "$prompt_outputs")" == "$prompt_delimiter" ]]

shadow_fixture="$fixture_root/shadow-capture"
mkdir -p "$shadow_fixture/target/classes/garden/ai"
touch "$shadow_fixture/target/classes/garden/ai/Main.class"
cat > "$shadow_fixture/fake-java" <<'RUNNER'
#!/usr/bin/env bash
set -euo pipefail
seed=""
while (( $# > 0 )); do
  if [[ "$1" == "--seed" ]]; then
    seed="$2"
    break
  fi
  shift
done
if [[ "$seed" == "99" ]]; then
  exit 7
fi
if [[ "$seed" == "88" ]]; then
  sleep 2
fi
if [[ "$seed" == "43" ]]; then
  sleep 0.2
else
  sleep 0.05
fi
printf '{"seed":%s,"status":"completed","final":{"total":1,"nutrients":1,"nutrientBuffer":1,"counts":{}},"maximumTotal":1}\n' "$seed"
RUNNER
chmod +x "$shadow_fixture/fake-java"
(
  cd "$shadow_fixture"
  SHADOW_SIMULATION_RUNNER="$shadow_fixture/fake-java" \
    SHADOW_SIMULATION_SEEDS=43,17 \
    "$repository_root/scripts/capture-shadow-simulation.sh" "$shadow_fixture/result.json" >/dev/null
)
[[ "$(jq -c '[.[].seed]' "$shadow_fixture/result.json")" == '[43,17]' ]]
if (
  cd "$shadow_fixture"
  SHADOW_SIMULATION_RUNNER="$shadow_fixture/fake-java" \
    SHADOW_SIMULATION_SEEDS=17,99 \
    "$repository_root/scripts/capture-shadow-simulation.sh" "$shadow_fixture/failed.json" >/dev/null 2>&1
); then
  echo "A failed parallel shadow seed incorrectly passed." >&2
  exit 1
fi
if (
  cd "$shadow_fixture"
  SHADOW_SIMULATION_RUNNER="$shadow_fixture/fake-java" \
    SHADOW_SIMULATION_SEEDS=17,88 \
    SHADOW_SIMULATION_TIMEOUT_SECONDS=1 \
    "$repository_root/scripts/capture-shadow-simulation.sh" "$shadow_fixture/timed-out.json" >/dev/null 2>&1
); then
  echo "A timed-out parallel shadow seed incorrectly passed." >&2
  exit 1
fi
mkdir -p "$shadow_fixture/scripts" "$shadow_fixture/agent/plans"
cp "$repository_root/scripts/capture-shadow-simulation.sh" "$repository_root/scripts/evaluate-shadow-candidate.sh" \
  "$repository_root/scripts/validate-agent-handoff.sh" "$repository_root/scripts/find-active-agent-plan.sh" \
  "$shadow_fixture/scripts/"
cat > "$shadow_fixture/.agent-run.json" <<'JSON'
{
  "title":"Capture failure fixture", "task":"Exercise structured rejection.", "why":"Regression coverage.",
  "summary":"Fixture.", "observations":"Fixture.", "next":"Fixture.",
  "expectedGardenEffect":"Increase beetles.", "pmDirection":"none",
  "evidence":{"bottleneck":"fixture","currentState":"fixture","verification":"fixture"},
  "evaluation":{"metric":"population.BEETLE","goal":"increase","requiredDelta":1},
  "codeMap":[], "requests":[], "state":{"immediateDirections":[],"constraints":[]}
}
JSON
if (
  cd "$shadow_fixture"
  SHADOW_SIMULATION_RUNNER="$shadow_fixture/fake-java" \
    SHADOW_SIMULATION_SEEDS=17,99 \
    SHADOW_EVALUATION_RESULT_FILE="$shadow_fixture/capture-failure-result.json" \
    scripts/evaluate-shadow-candidate.sh "$shadow_fixture/result.json" .agent-run.json "$shadow_fixture/capture-failure-candidate.json" >/dev/null 2>&1
); then
  echo "A failed candidate capture incorrectly passed evaluation." >&2
  exit 1
fi
jq -e '.passed == false and .reason == "candidate-shadow-capture-failed" and .observedDelta == null' \
  "$shadow_fixture/capture-failure-result.json" >/dev/null
rm -rf "$shadow_fixture"

mkdir -p "$fixture_root/scripts" "$fixture_root/agent/plans" "$fixture_root/artifacts"
for script in find-active-agent-plan.sh agent-substantive-changes.sh validate-agent-handoff.sh \
  extract-agent-handoff.sh inspect-agent-gemini-output.sh validate-agent-worktree.sh write-output-file.sh; do
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

defer_fixture="$fixture_root/defer-fixture"
mkdir -p "$defer_fixture/scripts" "$defer_fixture/src/main/java/example" "$defer_fixture/src/test/java/example"
cp "$repository_root/scripts/defer-shadow-rejection.sh" "$repository_root/scripts/validate-agent-handoff.sh" \
  "$repository_root/scripts/find-active-agent-plan.sh" "$defer_fixture/scripts/"
chmod +x "$defer_fixture/scripts/"*.sh
echo 'package example; class Change {}' > "$defer_fixture/src/main/java/example/Change.java"
(
  cd "$defer_fixture"
  git init -q
  git config user.name fixture
  git config user.email fixture@example.invalid
  git add scripts src/main/java/example/Change.java
  git commit -qm baseline
  echo 'package example; class Change { int rejected; }' > src/main/java/example/Change.java
  echo 'package example; class RejectedTest {}' > src/test/java/example/RejectedTest.java
  cat > .agent-run.json <<'JSON'
{
  "title":"Rejected fixture", "task":"Exercise deferral.", "why":"Regression coverage.",
  "summary":"Rejected source.", "observations":"No delta.", "next":"Use the evidence.",
  "expectedGardenEffect":"Increase beetles.", "pmDirection":"none",
  "evidence":{"bottleneck":"fixture","currentState":"fixture","verification":"fixture"},
  "evaluation":{"metric":"population.BEETLE","goal":"increase","requiredDelta":1},
  "codeMap":[{"path":"src/main/java/example/Change.java","description":"Fixture behavior."}],
  "requests":[], "state":{"immediateDirections":[],"constraints":[]}
}
JSON
  cat > shadow-result.json <<'JSON'
{"passed":false,"safetyPassed":true,"targetPassed":false,"metric":"population.BEETLE","goal":"increase","requiredDelta":1,"baselineAverage":1,"candidateAverage":1,"observedDelta":0,"seeds":[17,43]}
JSON
  SHADOW_BASELINE_FILE=shadow-result.json SHADOW_CANDIDATE_FILE=shadow-result.json \
    scripts/defer-shadow-rejection.sh shadow-result.json .agent-run.json agent/shadow-feedback.md >/dev/null
  git diff --quiet -- src/main/java/example/Change.java
  [[ ! -e src/test/java/example/RejectedTest.java ]]
  [[ ! -e .agent-run.json ]]
  grep -Fq '"observedDelta": 0' agent/shadow-feedback.md
  grep -Fq '## Baseline Shadow Runs' agent/shadow-feedback.md
  grep -Fq '## Candidate Shadow Runs' agent/shadow-feedback.md
  grep -Fq 'src/test/java/example/RejectedTest.java' agent/shadow-feedback.md
)

echo "Agent harness regression tests passed."
