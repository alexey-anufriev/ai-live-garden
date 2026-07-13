#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

output_file="$1"
mkdir -p "$(dirname "$output_file")"

tmp_log="$(mktemp)"
trap 'rm -f "$tmp_log"' EXIT

set +e
scripts/run-maven-tests-with-timeout.sh 2>&1 | tee "$tmp_log"
status="${PIPESTATUS[0]}"
set -e

{
  echo "- Command: \`scripts/run-maven-tests-with-timeout.sh\`"
  echo "- Exit code: ${status}"
  if [[ "$status" -eq 0 ]]; then
    echo "- Result: passed"
    echo
    echo "The committed project started this run with a passing Java test suite."
  else
    echo "- Result: failed"
    echo
    if [[ "$status" -eq 124 || "$status" -eq 137 ]]; then
      echo "The committed test suite exceeded ${MAVEN_TEST_TIMEOUT_SECONDS:-180} seconds and was interrupted. Treat this as a required baseline repair: find the runaway loop, population explosion, or blocked process before doing unrelated work."
    else
      echo "The committed project started this run with failing Java tests. The autonomous agent must repair this before choosing unrelated work."
    fi
    echo
    echo "## Output Tail"
    echo
    echo '```text'
    tail -n "${BASELINE_TEST_OUTPUT_LINES:-180}" "$tmp_log"
    echo '```'
  fi
} > "$output_file"

exit "$status"
