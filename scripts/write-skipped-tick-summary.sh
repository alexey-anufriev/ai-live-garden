#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 BASELINE_TEST_RESULT_FILE" >&2
  exit 2
fi

if [[ -z "${GITHUB_STEP_SUMMARY:-}" ]]; then
  echo "GITHUB_STEP_SUMMARY is required." >&2
  exit 2
fi

baseline_result_file="$1"

{
  echo "## Tick skipped"
  echo
  echo "Baseline Maven tests failed before the scheduled garden tick."
  echo "The workflow did not advance \`data/garden-state.txt\` or commit generated state."
  echo "The next autonomous agent run will receive the failing baseline and must repair it first."
  echo
  if [[ -f "$baseline_result_file" ]]; then
    cat "$baseline_result_file"
  fi
} >> "$GITHUB_STEP_SUMMARY"
