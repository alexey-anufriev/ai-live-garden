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
    echo "Partial-progress input not found: ${required_file}" >&2
    exit 1
  fi
done

partial_result="$(jq -c 'last(.[] | select(.accepted == true and .acceptance == "partial")) // empty' "$ledger_file")"
if [[ -z "$partial_result" ]]; then
  echo "Attempt ledger has no accepted partial-progress result." >&2
  exit 1
fi

mkdir -p "$(dirname "$output_file")"
temporary_output="$(mktemp "${RUNNER_TEMP:-/tmp}/partial-progress.XXXXXX")"
trap 'rm -f "$temporary_output"' EXIT

jq -r --argjson result "$partial_result" '
  "# Accepted Partial Autonomous Progress\n\n" +
  "The candidate was safe and moved the declared ecological metric in the correct direction, so it was merged after the final bounded attempt. It did not meet the full target. The next evolution run must treat this measured result as continuity evidence and either strengthen the proven mechanism or explicitly abandon it.\n\n" +
  "- Attempt: " + ($result.attempt | tostring) + " of 3\n" +
  "- Direction: " + .pmDirection + "\n" +
  "- Metric: `" + .evaluation.metric + "`\n" +
  "- Goal: `" + .evaluation.goal + "`\n" +
  "- Required delta: " + (.evaluation.requiredDelta | tostring) + "\n" +
  "- Observed delta: " + ($result.shadow.observedDelta | tostring) + "\n" +
  "- Baseline average: " + ($result.shadow.baselineAverage | tostring) + "\n" +
  "- Candidate average: " + ($result.shadow.candidateAverage | tostring) + "\n\n" +
  "## Implemented Mechanism\n\n" + .causalReach.mechanism + "\n\n" +
  "## Required Next Decision\n\n" +
  "Set `causalReach.previousFeedbackDecision` to `revise` when strengthening this proven causal path, or `abandon` when replacing it. Do not repeat the same source diff unchanged and do not lower acceptance criteria merely because the full target was missed.\n"
' "$handoff_file" > "$temporary_output"

mv "$temporary_output" "$output_file"
trap - EXIT
echo "Recorded accepted partial progress in ${output_file}."
