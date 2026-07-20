#!/usr/bin/env bash
set -euo pipefail

if (( $# != 2 )); then
  echo "Usage: $0 SHADOW_RESULT HANDOFF_FILE" >&2
  exit 2
fi

shadow_result="$1"
handoff_file="$2"

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

temporary_handoff="$(mktemp "${RUNNER_TEMP:-/tmp}/agent-handoff.XXXXXX")"
trap 'rm -f "$temporary_handoff"' EXIT
jq --slurpfile result "$shadow_result" '
  $result[0] as $shadow |
  .causalReach.preflight = {
    passed: $shadow.passed,
    observedDelta: $shadow.observedDelta
  } |
  .evidence.verification = (
    "Automated candidate verification: baselineAverage=" + ($shadow.baselineAverage | tostring) +
    ", candidateAverage=" + ($shadow.candidateAverage | tostring) +
    ", observedDelta=" + ($shadow.observedDelta | tostring) + "."
  )
' "$handoff_file" > "$temporary_handoff"
mv "$temporary_handoff" "$handoff_file"
trap - EXIT

