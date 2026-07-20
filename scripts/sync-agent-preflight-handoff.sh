#!/usr/bin/env bash
set -euo pipefail

if (( $# < 2 || $# > 4 )); then
  echo "Usage: $0 SHADOW_RESULT HANDOFF_FILE [full|experiment] [VERDICT]" >&2
  exit 2
fi

shadow_result="$1"
handoff_file="$2"
acceptance="${3:-full}"
verdict="${4:-}"

if [[ "$acceptance" != "full" && "$acceptance" != "experiment" ]]; then
  echo "Shadow acceptance must be full or experiment." >&2
  exit 2
fi

if ! jq -e '
  type == "object" and
  (.passed | type == "boolean") and
  (.baselineAverage | type == "number") and
  (.candidateAverage | type == "number") and
  (.observedDelta | type == "number")
' "$shadow_result" >/dev/null; then
  echo "Shadow result does not contain numeric differential evidence: ${shadow_result}" >&2
  exit 1
fi

if ! jq -e '.runMode == "evolution" and (.causalReach | type == "object")' "$handoff_file" >/dev/null; then
  echo "Only evolution handoffs with causalReach can receive target preflight evidence." >&2
  exit 1
fi

if [[ "$acceptance" == "experiment" ]]; then
  if ! jq -e '.safetyPassed == true and .targetPassed == false and (.observedDelta | type == "number")' "$shadow_result" >/dev/null; then
    echo "Experiment acceptance requires a safe measured target miss." >&2
    exit 1
  fi
  if ! grep -Eq '^(inert|partial-progress|wrong-direction)$' <<<"$verdict"; then
    echo "Experiment acceptance requires an inert, partial-progress, or wrong-direction verdict." >&2
    exit 1
  fi
fi

temporary_handoff="$(mktemp "${RUNNER_TEMP:-/tmp}/agent-handoff.XXXXXX")"
trap 'rm -f "$temporary_handoff"' EXIT
jq --arg acceptance "$acceptance" --arg verdict "$verdict" --slurpfile result "$shadow_result" '
  $result[0] as $shadow |
  .causalReach.preflight = (if $acceptance == "experiment" then {
    passed: false,
    acceptance: "experiment",
    safetyPassed: true,
    targetPassed: false,
    verdict: $verdict,
    observedDelta: $shadow.observedDelta
  } else {
    passed: $shadow.passed,
    observedDelta: $shadow.observedDelta
  } end) |
  .evidence.verification = (
    "Automated " + (if $acceptance == "experiment" then "safe experiment (" + $verdict + ")" else "candidate" end) +
    " verification: baselineAverage=" + ($shadow.baselineAverage | tostring) +
    ", candidateAverage=" + ($shadow.candidateAverage | tostring) +
    ", observedDelta=" + ($shadow.observedDelta | tostring) +
    ", requiredDelta=" + ($shadow.requiredDelta // 0 | tostring) + "."
  )
' "$handoff_file" > "$temporary_handoff"
mv "$temporary_handoff" "$handoff_file"
trap - EXIT
