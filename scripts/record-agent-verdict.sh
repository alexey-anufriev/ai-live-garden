#!/usr/bin/env bash
set -euo pipefail

if (( $# != 3 )); then
  echo "Usage: $0 ATTEMPT_LEDGER HANDOFF_FILE OUTPUT_FILE" >&2
  exit 2
fi

ledger_file="$1"
handoff_file="$2"
output_file="$3"

for required_file in "$ledger_file" "$handoff_file"; do
  if [[ ! -f "$required_file" ]]; then
    echo "Experiment-verdict input not found: ${required_file}" >&2
    exit 1
  fi
done

result="$(jq -c 'last(.[] | select(.accepted == true and (.shadow | type == "object"))) // empty' "$ledger_file")"
if [[ -z "$result" ]]; then
  echo "Attempt ledger has no accepted measured experiment." >&2
  exit 1
fi
if ! jq -e --argjson result "$result" '
  ($result.effectClassification | test("^(target-met|partial-progress|inert|wrong-direction)$")) and
  ($result.acceptance | test("^(full|experiment)$")) and
  ($result.shadow.safetyPassed == true) and
  (if $result.effectClassification == "target-met" then
     $result.acceptance == "full" and $result.shadow.targetPassed == true
   else
     $result.acceptance == "experiment" and $result.shadow.targetPassed == false
   end)
' "$ledger_file" >/dev/null; then
  echo "Accepted experiment result has an inconsistent verdict, acceptance class, or safety result." >&2
  exit 1
fi

mkdir -p "$(dirname "$output_file")"
temporary_output="$(mktemp "${RUNNER_TEMP:-/tmp}/agent-verdict.XXXXXX")"
trap 'rm -f "$temporary_output"' EXIT

jq -r --argjson result "$result" '
  ($result.effectClassification // "unmeasured") as $verdict |
  (if $verdict == "target-met" then
     "The expected differential was achieved. Keep the mechanism unless later living-state evidence contradicts it, then choose the next bounded milestone."
   elif $verdict == "partial-progress" then
     "The metric moved in the expected direction but missed the target. Revise and build on the proven causal path in the next run."
   elif $verdict == "inert" then
     "The code was safe but produced zero measured effect. Inspect the committed implementation, identify the inactive gate or clamp, and revise or revert it in the next run; do not add another disconnected mechanism."
   else
     "The code was safe but moved the metric in the wrong direction. Correct or revert this committed mechanism in the next run before adding another mechanism for the same objective."
   end) as $nextAction |
  "# Autonomous Experiment Verdict\n\n" +
  "This verdict evaluates the safe code committed by the previous autonomous run. Shadow evaluation is evidence for the next iteration, not a merge gate. The next agent must inspect the current implementation and explicitly choose to keep, revise, or revert it.\n\n" +
  "- Classification: `" + $verdict + "`\n" +
  "- Acceptance: `" + $result.acceptance + "`\n" +
  "- PM direction: `" + .pmDirection + "`\n" +
  "- Metric: `" + .evaluation.metric + "`\n" +
  "- Goal: `" + .evaluation.goal + "`\n" +
  "- Required delta: " + (.evaluation.requiredDelta | tostring) + "\n" +
  "- Observed delta: " + ($result.shadow.observedDelta | tostring) + "\n" +
  "- Baseline average: " + ($result.shadow.baselineAverage | tostring) + "\n" +
  "- Candidate average: " + ($result.shadow.candidateAverage | tostring) + "\n" +
  "- Safety passed: " + ($result.shadow.safetyPassed | tostring) + "\n" +
  "- Target passed: " + ($result.shadow.targetPassed | tostring) + "\n\n" +
  "## Implemented Hypothesis\n\n" + .causalReach.mechanism + "\n\n" +
  "## Harness Conclusion\n\n" + $nextAction + "\n\n" +
  "## Required Next Decision\n\n" +
  "Set `causalReach.previousFeedbackDecision` to `reuse`, `revise`, or `abandon` and explain the decision with current-state evidence. Because this code is already on main, inspect and change the implementation directly; there is no rejected branch to recover.\n"
' "$handoff_file" > "$temporary_output"

mv "$temporary_output" "$output_file"
trap - EXIT
echo "Recorded autonomous experiment verdict in ${output_file}."
