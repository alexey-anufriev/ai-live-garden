#!/usr/bin/env bash
set -euo pipefail

baseline_file="${1:-}"
handoff_file="${2:-.agent-run.json}"
candidate_file="${3:-${RUNNER_TEMP:-/tmp}/candidate-shadow.json}"
result_file="${SHADOW_EVALUATION_RESULT_FILE:-${RUNNER_TEMP:-/tmp}/shadow-evaluation-result.json}"
max_organisms="${SHADOW_MAX_ORGANISMS:-25000}"

if [[ -z "$baseline_file" || ! -f "$baseline_file" ]]; then
  echo "Usage: $0 BASELINE_FILE [HANDOFF_FILE] [CANDIDATE_FILE]" >&2
  exit 2
fi

scripts/validate-agent-handoff.sh "$handoff_file" >/dev/null
scripts/capture-shadow-simulation.sh "$candidate_file" >/dev/null

jq -n \
  --slurpfile baseline "$baseline_file" \
  --slurpfile candidate "$candidate_file" \
  --slurpfile handoff "$handoff_file" \
  --argjson maximum "$max_organisms" '
  def metric_value($report; $metric):
    if ($metric | startswith("population.")) then
      $report.final.counts[($metric | sub("^population\\."; ""))] // 0
    elif $metric == "totalOrganisms" then $report.final.total
    elif $metric == "nutrients" then $report.final.nutrients
    elif $metric == "nutrientBuffer" then $report.final.nutrientBuffer
    else 0 end;
  def average_metric($reports; $metric):
    ([$reports[] | metric_value(.; $metric)] | add / length);

  $baseline[0] as $base |
  $candidate[0] as $candidateRuns |
  $handoff[0].evaluation as $evaluation |
  ["BEETLE", "FOX", "FUNGUS", "ROOT_NETWORK"] as $criticalTypes |
  (average_metric($base; $evaluation.metric)) as $baselineValue |
  (average_metric($candidateRuns; $evaluation.metric)) as $candidateValue |
  ($candidateValue - $baselineValue) as $delta |
  ([range(0; $candidateRuns | length)] | all(. as $index |
    ($candidateRuns[$index].status == "completed") and
    ($candidateRuns[$index].maximumTotal <= $maximum) and
    ($candidateRuns[$index].maximumTotal <= (($base[$index].maximumTotal * 2) + 100)) and
    ($criticalTypes | all(. as $type |
      (($base[$index].final.counts[$type] // 0) == 0) or
      (($candidateRuns[$index].final.counts[$type] // 0) > 0)
    ))
  )) as $safe |
  (if $evaluation.goal == "increase" then $delta >= $evaluation.requiredDelta
   elif $evaluation.goal == "decrease" then $delta <= -$evaluation.requiredDelta
   elif $evaluation.goal == "preserve" then ($delta | fabs) <= $evaluation.requiredDelta
   elif $evaluation.goal == "pass" then true
   else false end) as $targetMet |
  {
    passed: ($safe and $targetMet),
    safetyPassed: $safe,
    targetPassed: $targetMet,
    metric: $evaluation.metric,
    goal: $evaluation.goal,
    requiredDelta: $evaluation.requiredDelta,
    baselineAverage: $baselineValue,
    candidateAverage: $candidateValue,
    observedDelta: $delta,
    seeds: [$candidateRuns[].seed]
  }
' > "$result_file"

cat "$result_file"
if ! jq -e '.passed == true' "$result_file" >/dev/null; then
  echo "Candidate shadow evaluation rejected the autonomous change." >&2
  exit 1
fi

echo "Candidate shadow evaluation passed."
