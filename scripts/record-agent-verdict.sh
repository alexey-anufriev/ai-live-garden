#!/usr/bin/env bash
set -euo pipefail

if (( $# < 3 || $# > 4 )); then
  echo "Usage: $0 ATTEMPT_LEDGER HANDOFF_FILE OUTPUT_FILE [PRIOR_FEEDBACK_FILE]" >&2
  exit 2
fi

ledger_file="$1"
handoff_file="$2"
output_file="$3"
prior_feedback_file="${4:-agent/shadow-feedback.md}"

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
  ($result.effectClassification | test("^(target-met|partial-progress|inert|measurement-saturated|wrong-direction)$")) and
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

previous_experiment='null'
if [[ -f "$prior_feedback_file" ]]; then
  prior_lineage="$(awk '
    /^<!-- AGENT-EXPERIMENT-LINEAGE-START -->$/ { capture = 1; next }
    /^<!-- AGENT-EXPERIMENT-LINEAGE-END -->$/ { exit }
    capture && !/^```/ { print }
  ' "$prior_feedback_file")"
  if [[ -n "$prior_lineage" ]] && jq -e '.current | type == "object"' >/dev/null 2>&1 <<<"$prior_lineage"; then
    previous_experiment="$(jq -c '.current' <<<"$prior_lineage")"
  fi
fi

candidate_commit="$(jq -r '.candidateCommit // empty' <<<"$result")"
changed_paths='[]'
if [[ -n "$candidate_commit" ]] && git cat-file -e "${candidate_commit}^{commit}" 2>/dev/null; then
  changed_paths="$(git diff-tree --no-commit-id --name-only -r "$candidate_commit" -- src/main src/test pom.xml data/garden-state.txt |
    jq -R . | jq -s .)"
fi

current_experiment="$(jq -cn \
  --arg commit "$candidate_commit" \
  --argjson paths "$changed_paths" \
  --argjson result "$result" \
  --slurpfile handoff "$handoff_file" '
    {
      commit: $commit,
      paths: $paths,
      mechanism: $handoff[0].causalReach.mechanism,
      metric: $handoff[0].evaluation.metric,
      goal: $handoff[0].evaluation.goal,
      requiredDelta: $handoff[0].evaluation.requiredDelta,
      classification: $result.effectClassification,
      observedDelta: $result.shadow.observedDelta,
      observation: ($result.shadow.observation // "terminal-observable")
    }
  ')"
lineage="$(jq -cn \
  --argjson current "$current_experiment" \
  --argjson previous "$previous_experiment" \
  --slurpfile handoff "$handoff_file" '
    def stalled($experiment):
      $experiment.classification == "inert" or
      $experiment.classification == "measurement-saturated" or
      $experiment.classification == "wrong-direction";
    ($handoff[0].causalReach.previousFeedbackDecision) as $decision |
    ($current.paths // []) as $currentPaths |
    ($previous.paths // []) as $previousPaths |
    (if $previous == null or ($previousPaths | length) == 0 then "unavailable"
     elif $decision == "abandon" then "abandoned"
     elif $decision == "reuse" or $decision == "revise" then
       if any($currentPaths[]; . as $path | $previousPaths | index($path)) then "matched" else "diverged" end
     else "unavailable"
     end) as $continuity |
    {
      current: $current,
      previous: $previous,
      responseToPrevious: $decision,
      continuity: $continuity,
      escalation:
        (if $previous != null and $continuity == "matched" and
            $current.metric == $previous.metric and $current.goal == $previous.goal and
            stalled($current) and stalled($previous)
         then "diagnose-or-abandon"
         else "none"
         end)
    }
  ')"

jq -r --argjson result "$result" --argjson lineage "$lineage" '
  ($result.effectClassification // "unmeasured") as $verdict |
  (if $verdict == "target-met" then
     "The expected differential was achieved. Keep the mechanism unless later living-state evidence contradicts it, then choose the next bounded milestone."
   elif $verdict == "partial-progress" then
     if ($result.shadow.observedDelta == 0 and (($result.shadow.trajectoryDelta // 0) != 0)) then
       "The final metric was saturated, but the bounded trajectory moved in the expected direction before that boundary. Revise and build on the proven causal path in the next run."
     else
       "The metric moved in the expected direction but missed the target. Revise and build on the proven causal path in the next run."
     end
   elif $verdict == "inert" then
     "The code was safe but produced zero measured effect. Inspect the committed implementation, identify the inactive gate or clamp, and revise or revert it in the next run; do not add another disconnected mechanism."
   elif $verdict == "measurement-saturated" then
     "The code was safe, but every baseline and candidate final value landed on the same 0/100 boundary. The final metric cannot distinguish this mechanism from the baseline; inspect the current flow and revise or abandon the existing mechanism rather than treating this as proof that it was inactive."
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
  "- Measurement: `" + ($result.shadow.observation // "terminal-observable") + "`\n" +
  "- Baseline initial values by seed: " + (($result.shadow.baselineInitialValues // []) | map(tostring) | join(", ")) + "\n" +
  "- Baseline final values by seed: " + (($result.shadow.baselineFinalValues // []) | map(tostring) | join(", ")) + "\n" +
  "- Candidate final values by seed: " + (($result.shadow.candidateFinalValues // []) | map(tostring) | join(", ")) + "\n" +
  "- Safety passed: " + ($result.shadow.safetyPassed | tostring) + "\n" +
  "- Target passed: " + ($result.shadow.targetPassed | tostring) + "\n\n" +
  "## Implemented Hypothesis\n\n" + .causalReach.mechanism + "\n\n" +
  (if ($result.shadow.trajectory // [] | length) > 0 then
     "## Bounded Trajectory Evidence\n\n" +
     "- Average trajectory delta: " + (($result.shadow.trajectoryDelta // 0) | tostring) + "\n" +
     "- Directional seed support: " + (($result.shadow.trajectoryDirectionalSupport.supporting // 0) | tostring) + " / " + (($result.shadow.trajectoryDirectionalSupport.total // 0) | tostring) + "\n" +
     ($result.shadow.trajectory | map(
       "- Seed " + (.seed | tostring) + ": baseline " +
       (.baseline | map(.value | tostring) | join(" → ")) +
       "; candidate " + (.candidate | map(.value | tostring) | join(" → "))
     ) | join("\n")) + "\n\n"
   else ""
   end) +
  "## Experiment Lineage\n\n" +
  "<!-- AGENT-EXPERIMENT-LINEAGE-START -->\n```json\n" + ($lineage | tojson) + "\n```\n<!-- AGENT-EXPERIMENT-LINEAGE-END -->\n\n" +
  "- Continuity: `" + $lineage.continuity + "`\n\n" +
  "- Escalation: `" + $lineage.escalation + "`\n\n" +
  "## Harness Conclusion\n\n" + $nextAction + "\n\n" +
  "## Required Next Decision\n\n" +
  "Set `causalReach.previousFeedbackDecision` to `reuse`, `revise`, or `abandon` and explain the decision with current-state evidence. The lineage retains only this experiment and its immediate predecessor. When reusing or revising, normally work on the listed prior path; when changing course, explicitly abandon it with evidence. Because this code is already on main, inspect and change the implementation directly; there is no rejected branch to recover.\n"
' "$handoff_file" > "$temporary_output"

mv "$temporary_output" "$output_file"
trap - EXIT
echo "Recorded autonomous experiment verdict in ${output_file}."
