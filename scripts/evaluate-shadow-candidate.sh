#!/usr/bin/env bash
set -euo pipefail

baseline_file="${1:-}"
handoff_file="${2:-.agent-run.json}"
candidate_file="${3:-${RUNNER_TEMP:-/tmp}/candidate-shadow.json}"
result_file="${SHADOW_EVALUATION_RESULT_FILE:-${RUNNER_TEMP:-/tmp}/shadow-evaluation-result.json}"
max_organisms="${SHADOW_MAX_ORGANISMS:-25000}"
evaluation_policy="${SHADOW_EVALUATION_POLICY:-target}"
extended_steps="${SHADOW_EXTENDED_DIAGNOSTIC_STEPS:-10}"
primary_steps="${SHADOW_SIMULATION_STEPS:-5}"

if [[ "$evaluation_policy" != "target" && "$evaluation_policy" != "safety" ]]; then
  echo "SHADOW_EVALUATION_POLICY must be target or safety." >&2
  exit 2
fi

if ! [[ "$extended_steps" =~ ^[1-9][0-9]*$ && "$primary_steps" =~ ^[1-9][0-9]*$ ]]; then
  echo "Shadow diagnostic horizons must be positive integers." >&2
  exit 2
fi

if [[ -z "$baseline_file" || ! -f "$baseline_file" ]]; then
  echo "Usage: $0 BASELINE_FILE [HANDOFF_FILE] [CANDIDATE_FILE]" >&2
  exit 2
fi

AGENT_HANDOFF_ALLOW_UNVERIFIED_PREFLIGHT=true scripts/validate-agent-handoff.sh "$handoff_file" >/dev/null
if [[ "${SHADOW_EVALUATION_TRAJECTORY_CAPTURED:-false}" == "true" ]]; then
  capture_command=(env SHADOW_SIMULATION_CAPTURE_TRAJECTORY=true scripts/capture-shadow-simulation.sh "$candidate_file")
else
  capture_command=(scripts/capture-shadow-simulation.sh "$candidate_file")
fi
if ! "${capture_command[@]}" >/dev/null; then
  jq -n \
    --slurpfile handoff "$handoff_file" \
    --arg policy "$evaluation_policy" '
    $handoff[0].evaluation as $evaluation |
    {
      passed: false,
      policy: $policy,
      safetyPassed: false,
      targetPassed: false,
      metric: $evaluation.metric,
      goal: $evaluation.goal,
      requiredDelta: $evaluation.requiredDelta,
      baselineAverage: null,
      candidateAverage: null,
      observedDelta: null,
      seeds: [],
      reason: "candidate-shadow-capture-failed"
    }
  ' > "$result_file"
  cat "$result_file"
  echo "Candidate shadow capture failed; the autonomous change could not be safely measured." >&2
  exit 1
fi

jq -n \
  --slurpfile baseline "$baseline_file" \
  --slurpfile candidate "$candidate_file" \
  --slurpfile handoff "$handoff_file" \
  --arg policy "$evaluation_policy" \
  --argjson maximum "$max_organisms" '
  def metric_value($report; $metric):
    if ($metric | startswith("population.")) then
      $report.final.counts[($metric | sub("^population\\."; ""))] // 0
    elif $metric == "totalOrganisms" then $report.final.total
    elif $metric == "nutrients" then $report.final.nutrients
    elif $metric == "nutrientBuffer" then $report.final.nutrientBuffer
    else 0 end;
  def initial_metric_value($report; $metric):
    if ($metric | startswith("population.")) then
      $report.initial.counts[($metric | sub("^population\\."; ""))] // 0
    elif $metric == "totalOrganisms" then $report.initial.total
    elif $metric == "nutrients" then $report.initial.nutrients
    elif $metric == "nutrientBuffer" then $report.initial.nutrientBuffer
    else 0 end;
  def trajectory_values($report; $metric):
    [($report.trajectory // [])[] | {step:.step, value:metric_value({final:.final}; $metric)}];
  def directional_checkpoints($trajectory; $goal):
    ($trajectory | ([.baseline | length, .candidate | length] | min) as $count |
      if $goal == "increase" then
        [range(0; $count) | select($trajectory.candidate[.].value > $trajectory.baseline[.].value)] | length
      elif $goal == "decrease" then
        [range(0; $count) | select($trajectory.candidate[.].value < $trajectory.baseline[.].value)] | length
      else 0 end);
  def average_metric($reports; $metric):
    ([$reports[] | metric_value(.; $metric)] | add / length);
  def bounded_environment_metric($metric):
    $metric == "nutrients" or $metric == "nutrientBuffer";

  $baseline[0] as $base |
  $candidate[0] as $candidateRuns |
  $handoff[0].evaluation as $evaluation |
  ["BEETLE", "FOX", "FUNGUS", "ROOT_NETWORK"] as $criticalTypes |
  (average_metric($base; $evaluation.metric)) as $baselineValue |
  (average_metric($candidateRuns; $evaluation.metric)) as $candidateValue |
  ($candidateValue - $baselineValue) as $delta |
  ([$base[] | metric_value(.; $evaluation.metric)]) as $baselineValues |
  ([$candidateRuns[] | metric_value(.; $evaluation.metric)]) as $candidateValues |
  ([$base[] | initial_metric_value(.; $evaluation.metric)]) as $baselineInitialValues |
  ([range(0; $candidateRuns | length) | . as $index |
    select(($base[$index].trajectory | type) == "array" and ($candidateRuns[$index].trajectory | type) == "array") |
    {
      seed: $candidateRuns[$index].seed,
      baseline: trajectory_values($base[$index]; $evaluation.metric),
      candidate: trajectory_values($candidateRuns[$index]; $evaluation.metric)
    }
  ]) as $trajectory |
  ($trajectory | map(. as $seedTrajectory |
    ([.baseline | length, .candidate | length] | min) as $checkpointCount |
    . + {
      delta: (([.candidate[].value] | add) - ([.baseline[].value] | add)),
      directionalCheckpoints: directional_checkpoints($seedTrajectory; $evaluation.goal),
      requiredDirectionalCheckpoints: (($checkpointCount + 1) / 2 | floor)
    }
  )) as $trajectoryBySeed |
  (if ($trajectoryBySeed | length) == 0 then 0
   else ($trajectoryBySeed | map(.delta) | add / length)
   end) as $trajectoryDelta |
  (if $evaluation.goal == "increase" then
     [$trajectoryBySeed[] | select(.delta > 0)] | length
  elif $evaluation.goal == "decrease" then
     [$trajectoryBySeed[] | select(.delta < 0)] | length
   else 0 end) as $trajectorySupportingSeeds |
  ([$trajectoryBySeed[] |
    select(.requiredDirectionalCheckpoints > 0 and .directionalCheckpoints >= .requiredDirectionalCheckpoints)
  ] | length) as $trajectoryPersistentSeeds |
  (bounded_environment_metric($evaluation.metric) and
    ([range(0; $candidateRuns | length)] | all(. as $index |
      ($baselineValues[$index] == $candidateValues[$index]) and
      (($baselineValues[$index] == 0) or ($baselineValues[$index] == 100))
    ))) as $terminalSaturated |
  ([range(0; $candidateRuns | length)] | all(. as $index |
    ($candidateRuns[$index].status == "completed") and
    ($candidateRuns[$index].maximumTotal <= $maximum) and
    ($candidateRuns[$index].maximumTotal <= (($base[$index].maximumTotal * 2) + 100)) and
    ($criticalTypes | all(. as $type |
      (($base[$index].final.counts[$type] // 0) == 0) or
      (($candidateRuns[$index].final.counts[$type] // 0) > 0)
    ))
  )) as $safe |
  (if $policy == "safety" then true
   elif $evaluation.goal == "increase" then $delta >= $evaluation.requiredDelta
   elif $evaluation.goal == "decrease" then $delta <= -$evaluation.requiredDelta
   elif $evaluation.goal == "preserve" then ($delta | fabs) <= $evaluation.requiredDelta
   elif $evaluation.goal == "pass" then true
   else false end) as $targetMet |
  {
    passed: ($safe and $targetMet),
    policy: $policy,
    safetyPassed: $safe,
    targetPassed: $targetMet,
    metric: $evaluation.metric,
    goal: $evaluation.goal,
    requiredDelta: $evaluation.requiredDelta,
    baselineAverage: $baselineValue,
    candidateAverage: $candidateValue,
    observedDelta: $delta,
    baselineFinalValues: $baselineValues,
    candidateFinalValues: $candidateValues,
    baselineInitialValues: $baselineInitialValues,
    trajectory: $trajectoryBySeed,
    trajectoryDelta: $trajectoryDelta,
    trajectoryDirectionalSupport: {
      supporting: $trajectorySupportingSeeds,
      persistent: $trajectoryPersistentSeeds,
      total: ($trajectoryBySeed | length)
    },
    observation: (if $terminalSaturated then "terminal-saturated" else "terminal-observable" end),
    seeds: [$candidateRuns[].seed]
  }
' > "$result_file"

if [[ "$evaluation_policy" == "target" && "${SHADOW_EVALUATION_TRAJECTORY_CAPTURED:-false}" != "true" ]] && \
    jq -e '.observation == "terminal-saturated" and (.trajectory | length) == 0' "$result_file" >/dev/null; then
  trajectory_baseline_file="$baseline_file"
  if ! jq -e 'all(.[]; (.trajectory | type) == "array")' "$trajectory_baseline_file" >/dev/null && \
      [[ -n "${SHADOW_BASELINE_CLASSES_DIR:-}" && -d "${SHADOW_BASELINE_CLASSES_DIR}" ]]; then
    trajectory_baseline_file="${result_file%.json}-baseline-trajectory.json"
    if ! SHADOW_SIMULATION_CAPTURE_TRAJECTORY=true \
        SHADOW_SIMULATION_CLASSES_DIR="$SHADOW_BASELINE_CLASSES_DIR" \
        scripts/capture-shadow-simulation.sh "$trajectory_baseline_file" >/dev/null; then
      echo "Baseline trajectory capture was unavailable; retaining terminal saturation evidence." >&2
      trajectory_baseline_file="$baseline_file"
    fi
  fi
  if jq -e 'all(.[]; (.trajectory | type) == "array")' "$trajectory_baseline_file" >/dev/null; then
    terminal_result_file="${result_file}.terminal"
    cp "$result_file" "$terminal_result_file"
    SHADOW_EVALUATION_TRAJECTORY_CAPTURED=true "$0" "$trajectory_baseline_file" "$handoff_file" "$candidate_file" || true
    if jq -e '.reason == "candidate-shadow-capture-failed"' "$result_file" >/dev/null; then
      cp "$terminal_result_file" "$result_file"
      echo "Candidate trajectory capture was unavailable; retaining terminal saturation evidence." >&2
    fi
    rm -f "$terminal_result_file"
  fi
fi

if [[ "$evaluation_policy" == "target" && "${SHADOW_EVALUATION_DIAGNOSTICS_DISABLED:-false}" != "true" ]] && \
    (( extended_steps > primary_steps )) && \
    [[ -n "${SHADOW_BASELINE_CLASSES_DIR:-}" && -d "${SHADOW_BASELINE_CLASSES_DIR}" ]] && \
    jq -e '.observation == "terminal-saturated" and (.trajectoryDirectionalSupport.total == 0 or (.trajectoryDirectionalSupport.supporting * 2 <= .trajectoryDirectionalSupport.total))' "$result_file" >/dev/null; then
  extended_baseline_file="${result_file%.json}-baseline-${extended_steps}-step.json"
  extended_candidate_file="${result_file%.json}-candidate-${extended_steps}-step.json"
  extended_result_file="${result_file%.json}-extended-${extended_steps}-step.json"
  if SHADOW_SIMULATION_STEPS="$extended_steps" \
      SHADOW_SIMULATION_CLASSES_DIR="$SHADOW_BASELINE_CLASSES_DIR" \
      scripts/capture-shadow-simulation.sh "$extended_baseline_file" >/dev/null; then
    SHADOW_EVALUATION_DIAGNOSTICS_DISABLED=true \
      SHADOW_SIMULATION_STEPS="$extended_steps" \
      SHADOW_EVALUATION_RESULT_FILE="$extended_result_file" \
      "$0" "$extended_baseline_file" "$handoff_file" "$extended_candidate_file" >/dev/null 2>&1 || true
    if jq -e '.observedDelta | type == "number"' "$extended_result_file" >/dev/null 2>&1; then
      jq --slurpfile extended "$extended_result_file" --argjson steps "$extended_steps" '
        . + {extendedHorizon: ($extended[0] | {
          steps: $steps,
          baselineAverage,
          candidateAverage,
          observedDelta,
          safetyPassed,
          targetPassed,
          observation
        })}
      ' "$result_file" > "${result_file}.extended" && mv "${result_file}.extended" "$result_file"
    else
      echo "Extended shadow diagnostic was unavailable; retaining bounded-window evidence." >&2
    fi
  else
    echo "Extended shadow diagnostic was unavailable; retaining bounded-window evidence." >&2
  fi
  rm -f "$extended_baseline_file" "$extended_candidate_file" "$extended_result_file"
fi

cat "$result_file"
if ! jq -e '.passed == true' "$result_file" >/dev/null; then
  echo "Candidate shadow evaluation did not meet the complete target-and-safety contract." >&2
  exit 1
fi

echo "Candidate shadow evaluation passed."
