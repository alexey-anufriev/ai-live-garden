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
mvn -B test 2>&1 | tee "$tmp_log"
status="${PIPESTATUS[0]}"
set -e

{
  echo "- Command: \`mvn -B test\`"
  echo "- Exit code: ${status}"
  if [[ "$status" -eq 0 ]]; then
    echo "- Result: passed"
    echo
    echo "The committed project started this run with a passing Java test suite."
  else
    echo "- Result: failed"
    echo
    echo "The committed project started this run with failing Java tests. The autonomous agent must repair this before choosing unrelated work."
    echo
    echo "## Output Tail"
    echo
    echo '```text'
    tail -n "${BASELINE_TEST_OUTPUT_LINES:-180}" "$tmp_log"
    echo '```'
  fi
} > "$output_file"

exit "$status"
