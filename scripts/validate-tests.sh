#!/usr/bin/env bash
set -euo pipefail

violations_file="${VALIDATE_REPAIR_VIOLATIONS_FILE:-}"
if [[ -z "$violations_file" ]]; then
  echo "VALIDATE_REPAIR_VIOLATIONS_FILE is required." >&2
  exit 2
fi

tmp_log="$(mktemp)"
trap 'rm -f "$tmp_log"' EXIT

set +e
mvn -B test 2>&1 | tee "$tmp_log"
status="${PIPESTATUS[0]}"
set -e

if [[ "$status" -eq 0 ]]; then
  : > "$violations_file"
  exit 0
fi

cp "$tmp_log" "$violations_file"
exit "$status"
