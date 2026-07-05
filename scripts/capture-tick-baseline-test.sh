#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

if [[ -z "${GITHUB_OUTPUT:-}" ]]; then
  echo "GITHUB_OUTPUT is required." >&2
  exit 2
fi

output_file="$1"

set +e
scripts/capture-maven-test-result.sh "$output_file"
status="$?"
set -e

if [[ "$status" -eq 0 ]]; then
  echo "passed=true" >> "$GITHUB_OUTPUT"
else
  echo "passed=false" >> "$GITHUB_OUTPUT"
fi

exit 0
