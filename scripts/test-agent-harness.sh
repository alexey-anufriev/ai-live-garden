#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "$0")/.." && pwd)"
fixture_root="$(mktemp -d)"
trap 'rm -rf "$fixture_root"' EXIT

assert_contains() {
  local expected="$1"
  local file="$2"

  if ! grep -Fq "$expected" "$file"; then
    echo "Expected ${file} to contain: ${expected}" >&2
    return 1
  fi
}

workflow_file="$repository_root/.github/workflows/evolve.yml"
for serialized_workflow in evolve.yml tick.yml pm.yml story.yml; do
  serialized_workflow_file="$repository_root/.github/workflows/$serialized_workflow"
  assert_contains 'group: ai-live-garden-main' "$serialized_workflow_file"
  assert_contains 'ref: ${{ github.ref_name }}' "$serialized_workflow_file"
done

while IFS= read -r workflow_script; do
  if [[ ! -f "$repository_root/$workflow_script" ]]; then
    echo "Workflow references a missing shell script: ${workflow_script}" >&2
    exit 1
  fi
  bash -n "$repository_root/$workflow_script"
done < <(grep -oE 'scripts/[a-zA-Z0-9._-]+[.]sh' "$workflow_file" | sort -u)

while IFS= read -r directly_invoked_script; do
  if [[ ! -x "$repository_root/$directly_invoked_script" ]]; then
    echo "Directly invoked workflow helper must be executable or called with bash: ${directly_invoked_script}" >&2
    exit 1
  fi
done < <(
  sed -nE \
    -e 's/^[[:space:]]*run:[[:space:]]+(scripts\/[a-zA-Z0-9._-]+[.]sh).*/\1/p' \
    -e 's/^[[:space:]]+(scripts\/[a-zA-Z0-9._-]+[.]sh).*/\1/p' \
    "$workflow_file" | sort -u
)

grep -Fq 'id: defer_shadow_rejection' "$workflow_file"
grep -Fq 'scripts/defer-shadow-rejection.sh' "$workflow_file"
grep -Fq 'id: commit_shadow_feedback' "$workflow_file"
grep -Fq 'id: defer_agent_incomplete' "$workflow_file"
grep -Fq 'id: publish_incomplete_candidate' "$workflow_file"
grep -Fq 'id: cleanup_incomplete_candidates' "$workflow_file"
grep -Fq 'id: shadow_repair_evaluation' "$workflow_file"
grep -Fq 'id: validation_policy' "$workflow_file"
grep -Fq 'id: repeated_candidate' "$workflow_file"
grep -Fq 'id: shadow_safety_evaluation' "$workflow_file"
grep -Fq 'id: candidate_validation' "$workflow_file"
grep -Fq 'id: publish_rejected_candidate' "$workflow_file"
grep -Fq 'id: cleanup_rejected_candidates' "$workflow_file"
grep -Fq 'id: cleanup_consumed_rejected_candidates' "$workflow_file"
grep -Fq 'run: scripts/defer-agent-incomplete.sh' "$workflow_file"
grep -Fq 'run: scripts/evaluate-shadow-repair.sh' "$workflow_file"
grep -Fq 'run: scripts/publish-rejected-candidate.sh' "$workflow_file"
grep -Fq 'run: scripts/detect-repeated-rejected-candidate.sh agent/shadow-feedback.md "${RUNNER_TEMP}/baseline-shadow.json"' "$workflow_file"
grep -Fq 'run: scripts/cleanup-rejected-candidate-branches.sh' "$workflow_file"
grep -Fq "steps.harness_contracts.outcome == 'success'" "$workflow_file"
grep -Fq "steps.setup_java.outcome == 'success'" "$workflow_file"
if grep -Eq 'build-(agent|shadow)-retry-prompt-output\.sh|Run Gemini corrective retry' "$workflow_file"; then
  echo "The workflow reintroduced a same-run corrective agent loop." >&2
  exit 1
fi

prompt_context="$fixture_root/agent-context.md"
prompt_metadata="$fixture_root/agent-context.metadata"
prompt_outputs="$fixture_root/prompt.outputs"
baseline_shadow="$fixture_root/baseline-shadow.json"
shadow_feedback="$fixture_root/shadow-feedback.md"
prompt_plans="$fixture_root/prompt-plans"
mkdir -p "$prompt_plans"
cat > "$prompt_plans/2026-01-02.md" <<'PLAN'
# Fixture Garden Direction

### A. Exercise the legacy plan prompt
PLAN
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
    AGENT_BASELINE_SHADOW_OUTCOME=success \
    AGENT_SHADOW_FEEDBACK_FILE="$shadow_feedback" \
    AGENT_PM_PLANS_DIR="$prompt_plans" \
    AGENT_PM_REFERENCE_DATE=2026-01-02 \
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
grep -Fq '## Previous Autonomous Feedback' "$prompt_outputs"
grep -Fq 'Previous candidate observed delta: 0.' "$prompt_outputs"
grep -Fq 'observedDelta = candidateAverage - baselineAverage' "$prompt_outputs"
grep -Fq 'mandatory decision input, not optional history' "$prompt_outputs"
grep -Fq '## Current Causal Reach Diagnostics' "$prompt_outputs"
grep -Fq 'causalReach' "$prompt_outputs"
assert_contains 'legacy plan has no machine-readable acceptance sidecar' "$prompt_outputs"
grep -Fq 'AGENT_CONTEXT_BYTES=' "$prompt_metadata"
grep -Fq "AGENT_CONTEXT_BASELINE_SHADOW_FILE=${baseline_shadow}" "$prompt_metadata"
grep -Fq 'AGENT_CONTEXT_BASELINE_SHADOW_OUTCOME=success' "$prompt_metadata"
grep -Fq "AGENT_CONTEXT_SHADOW_FEEDBACK_FILE=${shadow_feedback}" "$prompt_metadata"
prompt_delimiter="$(sed -n '1s/^text<<//p' "$prompt_outputs")"
[[ "$(tail -n 1 "$prompt_outputs")" == "$prompt_delimiter" ]]

cat > "$prompt_plans/2026-01-02.json" <<'JSON'
{"directions":[{"label":"A","shadowAcceptance":{"metric":"nutrients","goal":"increase","requiredDelta":1}}]}
JSON
structured_prompt_context="$fixture_root/structured-agent-context.md"
(
  cd "$repository_root"
  AGENT_CONTEXT_METADATA_FILE="$fixture_root/structured-agent-context.metadata" \
    AGENT_CONTEXT_RECENT_JOURNAL_LIMIT=1 \
    AGENT_BASELINE_SHADOW_FILE="$baseline_shadow" \
    AGENT_BASELINE_SHADOW_OUTCOME=success \
    AGENT_SHADOW_FEEDBACK_FILE="$shadow_feedback" \
    AGENT_PM_PLANS_DIR="$prompt_plans" \
    AGENT_PM_REFERENCE_DATE=2026-01-02 \
    GARDEN_OUTCOME_HISTORY_LIMIT=1 \
    scripts/build-agent-context.sh "$structured_prompt_context"
)
assert_contains "set \`acceptanceSource=pm\` only when using the plan sidecar's default shadow acceptance exactly" \
  "$structured_prompt_context"
if grep -Fq 'legacy plan has no machine-readable acceptance sidecar' "$structured_prompt_context"; then
  echo "Structured PM plan context incorrectly used the legacy-plan instruction." >&2
  exit 1
fi

causal_state="$fixture_root/causal-state.txt"
cat > "$causal_state" <<'STATE'
nutrients=6
nutrientBuffer=100
organism=moss-1|MOSS|1|1|1|nutrient-conserver\,quiet-hunger
organism=fox-1|FOX|1|1|1|quiet-hunger
STATE
[[ "$(scripts/count-garden-trait-carriers.sh nutrient-conserver "$causal_state")" == "1" ]]
[[ "$(scripts/count-garden-trait-carriers.sh nutrient-demand-regulator "$causal_state")" == "0" ]]
scripts/report-garden-causal-reach.sh "$causal_state" > "$fixture_root/causal-report.md"
grep -Fq 'nutrient-conserver: 1' "$fixture_root/causal-report.md"
grep -Fq 'clamp risk:' "$fixture_root/causal-report.md"

failed_shadow="$fixture_root/failed-baseline-shadow.json"
failed_context="$fixture_root/failed-context.md"
cat > "$failed_shadow" <<'JSON'
[{"seed":17,"requestedSteps":5,"completedSteps":0,"status":"timed-out","exitCode":124}]
JSON
(
  cd "$repository_root"
  AGENT_CONTEXT_METADATA_FILE="$fixture_root/failed-context.metadata" \
    AGENT_BASELINE_SHADOW_FILE="$failed_shadow" \
    AGENT_BASELINE_SHADOW_OUTCOME=failure \
    AGENT_SHADOW_FEEDBACK_FILE="$fixture_root/no-feedback.md" \
    scripts/build-agent-context.sh "$failed_context"
)
grep -Fq 'runMode=recovery' "$failed_context"
grep -Fq '"status": "timed-out"' "$failed_context"
grep -Fq 'tests/pass/0' "$failed_context"

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
jq -e '.[] | select(.seed == 99) | .status == "failed" and .exitCode == 7' \
  "$shadow_fixture/failed.json" >/dev/null
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
jq -e '.[] | select(.seed == 88) | .status == "timed-out" and .exitCode == 124' \
  "$shadow_fixture/timed-out.json" >/dev/null
mkdir -p "$shadow_fixture/scripts" "$shadow_fixture/agent/plans"
cp "$repository_root/scripts/capture-shadow-simulation.sh" "$repository_root/scripts/evaluate-shadow-candidate.sh" \
  "$repository_root/scripts/evaluate-shadow-repair.sh" \
  "$repository_root/scripts/validate-agent-handoff.sh" "$repository_root/scripts/find-active-agent-plan.sh" \
  "$shadow_fixture/scripts/"
cat > "$shadow_fixture/.agent-run.json" <<'JSON'
{
  "runMode":"evolution", "acceptanceSource":"agent",
  "title":"Capture failure fixture", "task":"Exercise structured rejection.", "why":"Regression coverage.",
  "summary":"Fixture.", "observations":"Fixture.", "next":"Fixture.",
  "expectedGardenEffect":"Increase beetles.", "pmDirection":"none",
  "evidence":{"bottleneck":"fixture","currentState":"fixture","verification":"preflight observedDelta pending"},
  "evaluation":{"metric":"population.BEETLE","goal":"increase","requiredDelta":1},
  "causalReach":{"mechanism":"global fixture","traits":[],"carrierBasis":"not-applicable","activeCarrierCount":0,"adoptionPath":"not-applicable","estimatedPhaseImpact":"fixture impact 1","clampRisk":"none","previousFeedbackDecision":"none","preflight":{"passed":false,"observedDelta":null}},
  "codeMap":[], "requests":[], "state":{"immediateDirections":[],"constraints":[]}
}
JSON
(
  cd "$shadow_fixture"
  SHADOW_SIMULATION_RUNNER="$shadow_fixture/fake-java" SHADOW_SIMULATION_SEEDS=17 \
    SHADOW_EVALUATION_POLICY=safety \
    SHADOW_EVALUATION_RESULT_FILE="$shadow_fixture/safety-result.json" \
    scripts/evaluate-shadow-candidate.sh "$shadow_fixture/result.json" .agent-run.json "$shadow_fixture/safety-candidate.json" >/dev/null
)
jq -e '.passed == true and .policy == "safety" and .targetPassed == true' \
  "$shadow_fixture/safety-result.json" >/dev/null
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
cat > "$shadow_fixture/repair-handoff.json" <<'JSON'
{
  "runMode":"repair", "acceptanceSource":"mode",
  "title":"Shadow repair fixture", "task":"Restore bounded shadow capture.", "why":"Regression coverage.",
  "summary":"Fixture.", "observations":"Fixture.", "next":"Fixture.",
  "expectedGardenEffect":"Future ticks complete within the bound.", "pmDirection":"none",
  "evidence":{"bottleneck":"fixture","currentState":"fixture","verification":"fixture"},
  "evaluation":{"metric":"tests","goal":"pass","requiredDelta":0},
  "codeMap":[], "requests":[], "state":{"immediateDirections":[],"constraints":[]}
}
JSON
(
  cd "$shadow_fixture"
  AGENT_BASELINE_SHADOW_OUTCOME=failure \
    SHADOW_SIMULATION_RUNNER="$shadow_fixture/fake-java" SHADOW_SIMULATION_SEEDS=17 \
    SHADOW_EVALUATION_RESULT_FILE="$shadow_fixture/repair-result.json" \
    bash scripts/evaluate-shadow-repair.sh repair-handoff.json repair-candidate.json >/dev/null
)
jq -e '.passed == true and .metric == "shadowSimulation" and .goal == "pass"' \
  "$shadow_fixture/repair-result.json" >/dev/null
rm -rf "$shadow_fixture"

mkdir -p "$fixture_root/scripts" "$fixture_root/agent/plans" "$fixture_root/artifacts"
for script in find-active-agent-plan.sh agent-substantive-changes.sh validate-agent-handoff.sh count-garden-trait-carriers.sh \
  derive-agent-validation-policy.sh report-complexity-budget.sh extract-agent-handoff.sh \
  inspect-agent-gemini-output.sh validate-agent-worktree.sh write-output-file.sh; do
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
cat > "$fixture_root/agent/plans/2026-07-08.json" <<'JSON'
{"directions":[
  {"label":"A","shadowAcceptance":{"metric":"population.BEETLE","goal":"increase","requiredDelta":1}},
  {"label":"B","shadowAcceptance":{"metric":"nutrients","goal":"preserve","requiredDelta":1}},
  {"label":"C","shadowAcceptance":{"metric":"population.FOX","goal":"increase","requiredDelta":1}},
  {"label":"D","shadowAcceptance":{"metric":"nutrientBuffer","goal":"decrease","requiredDelta":1}}
]}
JSON

cd "$fixture_root"
git init -q

handoff() {
  local direction="$1"
  local metric="$2"
  local goal="$3"
  local delta="$4"
  local output="$5"
  local mode="${6:-evolution}"
  local source="${7:-agent}"
  jq -n --arg direction "$direction" --arg metric "$metric" --arg goal "$goal" --argjson delta "$delta" --arg mode "$mode" --arg source "$source" '{
    runMode:$mode, acceptanceSource:$source,
    title:"Harness fixture", task:"Exercise the autonomous contract.", why:"Regression coverage.",
    summary:"Fixture.", observations:"Fixture.", next:"Fixture.", expectedGardenEffect:"Fixture effect.",
    pmDirection:$direction,
    evidence:{bottleneck:"fixture",currentState:"fixture",verification:"preflight observedDelta meets target"},
    evaluation:{metric:$metric,goal:$goal,requiredDelta:$delta},
    causalReach:(if $mode == "evolution" then {mechanism:"global fixture",traits:[],carrierBasis:"not-applicable",activeCarrierCount:0,adoptionPath:"not-applicable",estimatedPhaseImpact:"fixture impact 1",clampRisk:"none",previousFeedbackDecision:"none",preflight:{passed:true,observedDelta:(if $goal == "decrease" then -$delta elif $goal == "preserve" then 0 else $delta end)}} else null end),
    codeMap:[],requests:[],state:{immediateDirections:[],constraints:[]}
  }' > "$output"
}

[[ "$(AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/find-active-agent-plan.sh)" == "agent/plans/2026-07-08.md" ]]
[[ -z "$(AGENT_PM_REFERENCE_DATE=2026-07-09 scripts/find-active-agent-plan.sh)" ]]

handoff A population.BEETLE increase 1 handoff-active.json
AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/validate-agent-handoff.sh handoff-active.json >/dev/null
jq '.causalReach = {mechanism:"live trait fixture",traits:["nutrient-conserver"],carrierBasis:"existing",activeCarrierCount:1,adoptionPath:"one committed carrier",estimatedPhaseImpact:"1 versus demand 1",clampRisk:"none",previousFeedbackDecision:"none",preflight:{passed:true,observedDelta:1}}' \
  handoff-active.json > handoff-existing-carrier.json
AGENT_PM_REFERENCE_DATE=2026-07-08 AGENT_GARDEN_STATE_FILE="$causal_state" \
  scripts/validate-agent-handoff.sh handoff-existing-carrier.json >/dev/null
jq '.causalReach.traits = ["nutrient-demand-regulator"] | .causalReach.activeCarrierCount = 0' \
  handoff-existing-carrier.json > handoff-zero-carrier.json
if AGENT_PM_REFERENCE_DATE=2026-07-08 AGENT_GARDEN_STATE_FILE="$causal_state" \
    scripts/validate-agent-handoff.sh handoff-zero-carrier.json >/dev/null 2>&1; then
  echo "Evolution handoff incorrectly accepted a zero-carrier existing mechanism." >&2
  exit 1
fi
jq '.causalReach.preflight = {passed:false,observedDelta:0}' handoff-active.json > handoff-failed-preflight.json
if AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/validate-agent-handoff.sh handoff-failed-preflight.json >/dev/null 2>&1; then
  echo "Evolution handoff incorrectly accepted a failed differential preflight." >&2
  exit 1
fi
AGENT_HANDOFF_ALLOW_UNVERIFIED_PREFLIGHT=true AGENT_PM_REFERENCE_DATE=2026-07-08 \
  scripts/validate-agent-handoff.sh handoff-failed-preflight.json >/dev/null
mkdir -p agent
printf '# Deferred Shadow Evaluation Feedback\n' > agent/shadow-feedback.md
if AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/validate-agent-handoff.sh handoff-active.json >/dev/null 2>&1; then
  echo "Evolution handoff ignored supplied previous feedback." >&2
  exit 1
fi
jq '.causalReach.previousFeedbackDecision = "revise"' handoff-active.json > handoff-feedback-decision.json
AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/validate-agent-handoff.sh handoff-feedback-decision.json >/dev/null
rm agent/shadow-feedback.md
handoff A population.BEETLE increase 1 handoff-pm.json evolution pm
AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/validate-agent-handoff.sh handoff-pm.json >/dev/null
handoff A population.BEETLE increase 2 handoff-pm-mismatch.json evolution pm
if AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/validate-agent-handoff.sh handoff-pm-mismatch.json >/dev/null 2>&1; then
  echo "PM acceptance source incorrectly accepted a target that differed from the plan sidecar." >&2
  exit 1
fi
mv agent/plans/2026-07-08.json legacy-plan-sidecar.json
AGENT_PM_REFERENCE_DATE=2026-07-08 scripts/validate-agent-handoff.sh handoff-pm-mismatch.json >/dev/null
mv legacy-plan-sidecar.json agent/plans/2026-07-08.json

handoff none population.BEETLE increase 1 handoff-stale.json
AGENT_PM_REFERENCE_DATE=2026-07-09 scripts/validate-agent-handoff.sh handoff-stale.json >/dev/null

handoff none tests pass 0 handoff-repair.json repair mode
AGENT_PM_REFERENCE_DATE=2026-07-08 AGENT_BASELINE_TEST_OUTCOME=failure \
  scripts/validate-agent-handoff.sh handoff-repair.json >/dev/null
AGENT_PM_REFERENCE_DATE=2026-07-08 AGENT_BASELINE_SHADOW_OUTCOME=failure \
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
rm -rf artifacts inspect.outputs

git config user.name fixture
git config user.email fixture@example.invalid
printf 'artifacts/\n' > .gitignore
git add -A
git commit -qm baseline
mkdir -p artifacts
printf '{}\n' > artifacts/stdout.log
GITHUB_OUTPUT="$fixture_root/artifacts/inspect-empty.outputs" AGENT_PM_REFERENCE_DATE=2026-07-08 \
  scripts/inspect-agent-gemini-output.sh artifacts >/dev/null
grep -Fxq 'valid_handoff=false' artifacts/inspect-empty.outputs
grep -Fxq 'retry_required=true' artifacts/inspect-empty.outputs
if ! grep -Fxq 'noop_reason=agent-returned-no-valid-handoff-or-changes' artifacts/inspect-empty.outputs; then
  echo "An empty agent response received the wrong deferral classification:" >&2
  cat artifacts/inspect-empty.outputs >&2
  git status --short -uall >&2
  exit 1
fi
rm -rf artifacts

echo 'temporary helper' > scratch.py
if VALIDATE_AGENT_WORKTREE_SCOPE=changed scripts/validate-agent-worktree.sh >/dev/null 2>&1; then
  echo "Root Python scratch file incorrectly passed worktree policy." >&2
  exit 1
fi
rm scratch.py

if VALIDATE_AGENT_WORKTREE_SCOPE=changed scripts/validate-agent-worktree.sh >/dev/null 2>&1; then
  echo "Generated-only fixture incorrectly passed substantive-change policy." >&2
  exit 1
fi

mkdir -p src/main/java/example
echo 'package example;' > src/main/java/example/Change.java
scripts/agent-substantive-changes.sh | grep -Fxq 'src/main/java/example/Change.java'
VALIDATE_AGENT_WORKTREE_SCOPE=changed scripts/validate-agent-worktree.sh >/dev/null
mkdir -p artifacts
jq -n --rawfile response handoff-pm-mismatch.json \
  '{response:("AGENT_RUN_JSON_START\n" + $response + "AGENT_RUN_JSON_END")}' > artifacts/stdout.log
GITHUB_OUTPUT=inspect-invalid.outputs AGENT_PM_REFERENCE_DATE=2026-07-08 \
  scripts/inspect-agent-gemini-output.sh artifacts >/dev/null
grep -Fxq 'valid_handoff=false' inspect-invalid.outputs
grep -Fxq 'handoff_candidate=true' inspect-invalid.outputs
grep -Fxq 'noop_reason=changes-with-invalid-handoff' inspect-invalid.outputs
if ! grep -Fq 'handoff_validation_reason=Agent evaluation must exactly match' inspect-invalid.outputs; then
  echo "Invalid handoff diagnostics did not expose the semantic validator reason:" >&2
  cat inspect-invalid.outputs >&2
  exit 1
fi
rm -rf artifacts inspect-invalid.outputs
GITHUB_OUTPUT=policy-evolution.outputs AGENT_PM_REFERENCE_DATE=2026-07-08 \
  scripts/derive-agent-validation-policy.sh handoff-active.json >/dev/null
grep -Fxq 'run_mode=evolution' policy-evolution.outputs
grep -Fxq 'shadow_policy=target' policy-evolution.outputs
grep -Fxq 'advance_garden=true' policy-evolution.outputs

GITHUB_OUTPUT=policy-repair-production.outputs AGENT_PM_REFERENCE_DATE=2026-07-08 \
  AGENT_BASELINE_TEST_OUTCOME=failure scripts/derive-agent-validation-policy.sh handoff-repair.json >/dev/null
grep -Fxq 'shadow_policy=safety' policy-repair-production.outputs
grep -Fxq 'advance_garden=false' policy-repair-production.outputs

rm src/main/java/example/Change.java
mkdir -p src/test/java/example
echo 'package example;' > src/test/java/example/DiagnosticTest.java
GITHUB_OUTPUT=policy-repair-tests.outputs AGENT_PM_REFERENCE_DATE=2026-07-08 \
  AGENT_BASELINE_TEST_OUTCOME=failure scripts/derive-agent-validation-policy.sh handoff-repair.json >/dev/null
grep -Fxq 'shadow_policy=skip' policy-repair-tests.outputs

handoff none tests pass 0 handoff-diagnostic.json diagnostic mode
GITHUB_OUTPUT=policy-diagnostic.outputs AGENT_PM_REFERENCE_DATE=2026-07-08 \
  scripts/derive-agent-validation-policy.sh handoff-diagnostic.json >/dev/null
grep -Fxq 'shadow_policy=skip' policy-diagnostic.outputs
grep -Fxq 'advance_garden=false' policy-diagnostic.outputs

rm src/test/java/example/DiagnosticTest.java
echo 'package example; class Change {}' > src/main/java/example/Change.java
handoff none totalOrganisms preserve 0 handoff-maintenance.json maintenance mode
GITHUB_OUTPUT=policy-maintenance.outputs AGENT_PM_REFERENCE_DATE=2026-07-08 \
  AGENT_SOURCE_FILE_LINE_BUDGET=0 scripts/derive-agent-validation-policy.sh handoff-maintenance.json >/dev/null
grep -Fxq 'shadow_policy=safety' policy-maintenance.outputs
grep -Fxq 'advance_garden=false' policy-maintenance.outputs

defer_fixture="$fixture_root/defer-fixture"
defer_remote="$fixture_root/defer-remote.git"
git init --bare -q "$defer_remote"
mkdir -p "$defer_fixture/scripts" "$defer_fixture/src/main/java/example" "$defer_fixture/src/test/java/example"
cp "$repository_root/scripts/defer-shadow-rejection.sh" "$repository_root/scripts/validate-agent-handoff.sh" \
  "$repository_root/scripts/find-active-agent-plan.sh" "$repository_root/scripts/publish-rejected-candidate.sh" \
  "$repository_root/scripts/cleanup-rejected-candidate-branches.sh" \
  "$repository_root/scripts/detect-repeated-rejected-candidate.sh" "$defer_fixture/scripts/"
chmod +x "$defer_fixture/scripts/"*.sh
echo 'package example; class Change {}' > "$defer_fixture/src/main/java/example/Change.java"
(
  cd "$defer_fixture"
  git init -q
  git config user.name fixture
  git config user.email fixture@example.invalid
  git add scripts src/main/java/example/Change.java
  git commit -qm baseline
  git remote add origin "$defer_remote"
  git push -q -u origin HEAD:main
  echo 'package example; class Change { int rejected; }' > src/main/java/example/Change.java
  echo 'package example; class RejectedTest {}' > src/test/java/example/RejectedTest.java
  cat > .agent-run.json <<'JSON'
{
  "runMode":"evolution", "acceptanceSource":"agent",
  "title":"Rejected fixture", "task":"Exercise deferral.", "why":"Regression coverage.",
  "summary":"Rejected source.", "observations":"No delta.", "next":"Use the evidence.",
  "expectedGardenEffect":"Increase beetles.", "pmDirection":"none",
  "evidence":{"bottleneck":"fixture","currentState":"fixture","verification":"preflight observedDelta 1"},
  "evaluation":{"metric":"population.BEETLE","goal":"increase","requiredDelta":1},
  "causalReach":{"mechanism":"global fixture","traits":[],"carrierBasis":"not-applicable","activeCarrierCount":0,"adoptionPath":"not-applicable","estimatedPhaseImpact":"fixture impact 1","clampRisk":"none","previousFeedbackDecision":"none","preflight":{"passed":true,"observedDelta":1}},
  "codeMap":[{"path":"src/main/java/example/Change.java","description":"Fixture behavior."}],
  "requests":[], "state":{"immediateDirections":[],"constraints":[]}
}
JSON
  cat > shadow-runs.json <<'JSON'
[{"seed":17,"requestedSteps":5,"completedSteps":5,"status":"completed","initial":{"total":1,"counts":{"BEETLE":1}},"final":{"total":1,"counts":{"BEETLE":1}},"minimumTotal":1,"maximumTotal":1}]
JSON
  GITHUB_OUTPUT=publish.outputs GITHUB_RUN_ID=12345 GITHUB_RUN_ATTEMPT=2 \
    bash scripts/publish-rejected-candidate.sh >/dev/null
  rejected_branch="$(sed -n 's/^branch=//p' publish.outputs)"
  rejected_commit="$(sed -n 's/^commit=//p' publish.outputs)"
  [[ "$rejected_branch" == "agent-rejected/12345-2" ]]
  [[ "$rejected_commit" =~ ^[0-9a-f]{40}$ ]]
  git --git-dir="$defer_remote" show "${rejected_commit}:src/main/java/example/Change.java" | \
    grep -Fq 'int rejected'
  git --git-dir="$defer_remote" show "${rejected_commit}:src/test/java/example/RejectedTest.java" | \
    grep -Fq 'RejectedTest'
  if git --git-dir="$defer_remote" cat-file -e "${rejected_commit}:.agent-run.json" 2>/dev/null; then
    echo "Rejected candidate branch incorrectly included the machine handoff." >&2
    exit 1
  fi
  cat > shadow-result.json <<'JSON'
{"passed":false,"safetyPassed":true,"targetPassed":false,"metric":"population.BEETLE","goal":"increase","requiredDelta":1,"baselineAverage":1,"candidateAverage":1,"observedDelta":0,"seeds":[17,43]}
JSON
  REJECTED_CANDIDATE_BRANCH="$rejected_branch" REJECTED_CANDIDATE_COMMIT="$rejected_commit" \
    SHADOW_BASELINE_FILE=shadow-runs.json SHADOW_CANDIDATE_FILE=shadow-runs.json \
    scripts/defer-shadow-rejection.sh shadow-result.json .agent-run.json agent/shadow-feedback.md >/dev/null
  git diff --quiet -- src/main/java/example/Change.java
  [[ ! -e src/test/java/example/RejectedTest.java ]]
  [[ ! -e .agent-run.json ]]
  grep -Fq '"observedDelta": 0' agent/shadow-feedback.md
  grep -Fq "Branch: \`${rejected_branch}\`" agent/shadow-feedback.md
  grep -Fq "Commit: \`${rejected_commit}\`" agent/shadow-feedback.md
  grep -Fq '## Baseline Shadow Runs' agent/shadow-feedback.md
  grep -Fq '## Candidate Shadow Runs' agent/shadow-feedback.md
  grep -Fq '## What Acceptance Required' agent/shadow-feedback.md
  grep -Fq 'The declared ecological target was missed' agent/shadow-feedback.md
  if grep -Eq 'codeMap|## Rejected Change Paths|## Rejected Change Summary' agent/shadow-feedback.md; then
    echo "Compact rejection feedback duplicated source details preserved on the branch." >&2
    exit 1
  fi
  echo 'package example; class Change { int rejected; }' > src/main/java/example/Change.java
  mkdir -p src/test/java/example
  echo 'package example; class RejectedTest {}' > src/test/java/example/RejectedTest.java
  if REPEATED_CANDIDATE_RESULT_FILE=repeated-result.json \
      scripts/detect-repeated-rejected-candidate.sh agent/shadow-feedback.md shadow-runs.json >/dev/null 2>&1; then
    echo "An identical previous rejection was not detected." >&2
    exit 1
  fi
  jq -e '.reason == "repeated-previous-rejection" and .previousRejectedCommit == $commit' \
    --arg commit "$rejected_commit" repeated-result.json >/dev/null
  jq '.[0].initial.total = 2' shadow-runs.json > changed-shadow-runs.json
  scripts/detect-repeated-rejected-candidate.sh agent/shadow-feedback.md changed-shadow-runs.json >/dev/null
  echo 'package example; class Change { int revised; }' > src/main/java/example/Change.java
  scripts/detect-repeated-rejected-candidate.sh agent/shadow-feedback.md shadow-runs.json >/dev/null
  git restore src/main/java/example/Change.java
  rm -f src/test/java/example/RejectedTest.java repeated-result.json changed-shadow-runs.json
  bash scripts/cleanup-rejected-candidate-branches.sh "$rejected_branch" >/dev/null
  git ls-remote --exit-code --heads origin "refs/heads/${rejected_branch}" >/dev/null
  bash scripts/cleanup-rejected-candidate-branches.sh >/dev/null
  if git ls-remote --exit-code --heads origin "refs/heads/${rejected_branch}" >/dev/null 2>&1; then
    echo "Consumed rejected candidate branch was not removed." >&2
    exit 1
  fi
)

incomplete_fixture="$fixture_root/incomplete-fixture"
incomplete_remote="$fixture_root/incomplete-remote.git"
git init --bare -q "$incomplete_remote"
mkdir -p "$incomplete_fixture/scripts" "$incomplete_fixture/src/main/java/example" \
  "$incomplete_fixture/gemini-artifacts" "$incomplete_fixture/agent"
cp "$repository_root/scripts/defer-agent-incomplete.sh" "$repository_root/scripts/publish-rejected-candidate.sh" \
  "$repository_root/scripts/cleanup-rejected-candidate-branches.sh" "$incomplete_fixture/scripts/"
chmod +x "$incomplete_fixture/scripts/"*.sh
echo 'package example; class Change {}' > "$incomplete_fixture/src/main/java/example/Change.java"
printf '# Deferred Shadow Evaluation Feedback\n\n- Branch: `agent-rejected/prior-1`\n' \
  > "$incomplete_fixture/agent/shadow-feedback.md"
(
  cd "$incomplete_fixture"
  git init -q
  git config user.name fixture
  git config user.email fixture@example.invalid
  git add scripts src/main/java/example/Change.java agent/shadow-feedback.md
  git commit -qm baseline
  git remote add origin "$incomplete_remote"
  git push -q -u origin HEAD:main
  echo 'package example; class Change { int partial; }' > src/main/java/example/Change.java
  mkdir -p unrelated
  echo 'partial scratch' > unrelated/scratch.txt
  printf '{"response":"partial agent response","stats":{"tools":{"totalCalls":2}}}\n' \
    > gemini-artifacts/stdout.log
  GITHUB_OUTPUT=publish-incomplete.outputs GITHUB_RUN_ID=67890 GITHUB_RUN_ATTEMPT=1 \
    scripts/publish-rejected-candidate.sh >/dev/null
  incomplete_branch="$(sed -n 's/^branch=//p' publish-incomplete.outputs)"
  incomplete_commit="$(sed -n 's/^commit=//p' publish-incomplete.outputs)"
  INCOMPLETE_CANDIDATE_BRANCH="$incomplete_branch" INCOMPLETE_CANDIDATE_COMMIT="$incomplete_commit" \
    HANDOFF_VALIDATION_REASON='Fixture handoff validation failed.' \
    scripts/defer-agent-incomplete.sh gemini-artifacts changes-with-invalid-handoff \
      agent/shadow-feedback.md >/dev/null
  git diff --quiet -- src/main/java/example/Change.java
  [[ ! -e unrelated/scratch.txt ]]
  git --git-dir="$incomplete_remote" show "${incomplete_commit}:src/main/java/example/Change.java" | grep -Fq 'int partial'
  grep -Fq 'changes-with-invalid-handoff' agent/shadow-feedback.md
  grep -Fq 'Fixture handoff validation failed.' agent/shadow-feedback.md
  grep -Fq "Branch: \`${incomplete_branch}\`" agent/shadow-feedback.md
  grep -Fq 'Branch: `agent-rejected/prior-1`' agent/shadow-feedback.md
  grep -Fq '## Latest Incomplete Attempt' agent/shadow-feedback.md
  grep -Fq '## Prior Feedback' agent/shadow-feedback.md
  if grep -Fq '## Subsequent Incomplete Attempt' agent/shadow-feedback.md; then
    echo "Incomplete feedback retained the obsolete append marker." >&2
    exit 1
  fi
  grep -Fq 'src/main/java/example/Change.java' agent/shadow-feedback.md
  grep -Fq 'unrelated/scratch.txt' agent/shadow-feedback.md
  grep -Fq 'partial agent response' agent/shadow-feedback.md
  scripts/cleanup-rejected-candidate-branches.sh "$incomplete_branch" agent-rejected/prior-1 >/dev/null
  git ls-remote --exit-code --heads origin "refs/heads/${incomplete_branch}" >/dev/null
)

observation_fixture="$fixture_root/observation-fixture"
mkdir -p "$observation_fixture/scripts"
cp "$repository_root/scripts/check-agent-observation-window.sh" "$observation_fixture/scripts/"
chmod +x "$observation_fixture/scripts/check-agent-observation-window.sh"
(
  cd "$observation_fixture"
  git init -q
  git config user.name fixture
  git config user.email fixture@example.invalid
  touch state
  git add scripts state
  git commit -qm 'feat: autonomous garden evolution fixture'
  GITHUB_OUTPUT="$observation_fixture/cooldown.outputs" AGENT_MIN_TICKS_BETWEEN_RUNS=3 \
    scripts/check-agent-observation-window.sh >/dev/null
  grep -Fxq 'ready=false' cooldown.outputs
)

plan_fixture="$fixture_root/plan-fixture"
mkdir -p "$plan_fixture/scripts" "$plan_fixture/agent/plans"
cp "$repository_root/scripts/validate-project-plan.sh" "$repository_root/scripts/render-project-plan.sh" \
  "$repository_root/scripts/validate-project-plan-worktree.sh" "$plan_fixture/scripts/"
chmod +x "$plan_fixture/scripts/"*.sh
(
  cd "$plan_fixture"
  git init -q
  git config user.name fixture
  git config user.email fixture@example.invalid
  touch agent/plans/.gitkeep
  git add .
  git commit -qm baseline
  jq -n '{
    date:"2026-07-19",
    review:{previousPlanDate:"none",overallMark:"unclear",summary:"No prior evidence.",results:[]},
    thesis:"Four bounded ecological outcomes provide useful autonomous directions today.",
    stateSignals:["Fixture state signal."],
    directions:["A","B","C","D"] | map({
      label:., title:(. + " direction"), why:"Fixture evidence.",
      expectedGardenEffect:"A future bounded tick changes observably.",
      acceptanceSignal:"The bounded simulation exposes the expected outcome.",
      shadowAcceptance:{metric:"totalOrganisms",goal:"preserve",requiredDelta:1},
      avoid:"Avoid unrelated work."
    }),
    antiPatterns:["Unbounded work."]
  }' > .project-plan.json
  PROJECT_PLAN_EXPECTED_DATE=2026-07-19 scripts/validate-project-plan.sh .project-plan.json >/dev/null
  PROJECT_PLAN_EXPECTED_DATE=2026-07-19 scripts/render-project-plan.sh .project-plan.json >/dev/null
  [[ -f agent/plans/2026-07-19.md ]]
  [[ -f agent/plans/2026-07-19.json ]]
  jq -e '.directions[0].shadowAcceptance.goal == "preserve"' agent/plans/2026-07-19.json >/dev/null
  scripts/validate-project-plan-worktree.sh >/dev/null
)

echo "Agent harness regression tests passed."
