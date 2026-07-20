#!/usr/bin/env bash
set -euo pipefail

if (( $# < 2 || $# > 3 )); then
  echo "Usage: $0 SHADOW_RESULT HANDOFF_FILE [full|partial]" >&2
  exit 2
fi

shadow_result="$1"
handoff_file="$2"
acceptance="${3:-full}"

if [[ "$acceptance" != "full" && "$acceptance" != "partial" ]]; then
  echo "Shadow acceptance must be full or partial." >&2
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

if [[ "$acceptance" == "partial" ]]; then
  if ! jq -e '.safetyPassed == true and .targetPassed == false and (.observedDelta != 0)' "$shadow_result" >/dev/null; then
    echo "Partial acceptance requires a safe, nonzero target miss." >&2
    exit 1
  fi
  goal="$(jq -r '.evaluation.goal' "$handoff_file")"
  delta="$(jq -r '.observedDelta' "$shadow_result")"
  case "$goal" in
    increase) awk -v delta="$delta" 'BEGIN { exit !(delta > 0) }' ;;
    decrease) awk -v delta="$delta" 'BEGIN { exit !(delta < 0) }' ;;
    *)
      echo "Partial acceptance is only valid for increase or decrease targets." >&2
      exit 1
      ;;
  esac
fi

temporary_handoff="$(mktemp "${RUNNER_TEMP:-/tmp}/agent-handoff.XXXXXX")"
trap 'rm -f "$temporary_handoff"' EXIT
jq --arg acceptance "$acceptance" --slurpfile result "$shadow_result" '
  $result[0] as $shadow |
  .causalReach.preflight = (if $acceptance == "partial" then {
    passed: true,
    acceptance: "partial",
    targetPassed: false,
    observedDelta: $shadow.observedDelta
  } else {
    passed: $shadow.passed,
    observedDelta: $shadow.observedDelta
  } end) |
  .evidence.verification = (
    "Automated " + (if $acceptance == "partial" then "safe partial-progress" else "candidate" end) +
    " verification: baselineAverage=" + ($shadow.baselineAverage | tostring) +
    ", candidateAverage=" + ($shadow.candidateAverage | tostring) +
    ", observedDelta=" + ($shadow.observedDelta | tostring) +
    ", requiredDelta=" + ($shadow.requiredDelta // 0 | tostring) + "."
  )
' "$handoff_file" > "$temporary_handoff"
mv "$temporary_handoff" "$handoff_file"
trap - EXIT
