#!/usr/bin/env bash
set -euo pipefail

handoff_file="${1:-.agent-run.json}"
candidate_file="${2:-${RUNNER_TEMP:-/tmp}/candidate-shadow-repair.json}"
result_file="${SHADOW_EVALUATION_RESULT_FILE:-${RUNNER_TEMP:-/tmp}/shadow-repair-result.json}"

scripts/validate-agent-handoff.sh "$handoff_file" >/dev/null

capture_passed="true"
if ! scripts/capture-shadow-simulation.sh "$candidate_file" >/dev/null; then
  capture_passed="false"
fi

jq -n \
  --slurpfile candidate "$candidate_file" \
  --argjson passed "$capture_passed" '
  {
    passed: $passed,
    safetyPassed: $passed,
    targetPassed: $passed,
    metric: "shadowSimulation",
    goal: "pass",
    requiredDelta: 0,
    baselineAverage: null,
    candidateAverage: null,
    observedDelta: null,
    seeds: [$candidate[0][].seed],
    reason: (if $passed then "shadow-operability-restored" else "candidate-shadow-capture-failed" end)
  }
' > "$result_file"

cat "$result_file"
if [[ "$capture_passed" != "true" ]]; then
  echo "Candidate did not restore bounded shadow simulation operability." >&2
  exit 1
fi

echo "Candidate restored bounded shadow simulation operability."
