#!/usr/bin/env bash
set -euo pipefail

if (( $# != 2 )); then
  echo "Usage: $0 OUTPUT_NAME FILE" >&2
  exit 2
fi

if [[ -z "${GITHUB_OUTPUT:-}" ]]; then
  echo "GITHUB_OUTPUT is not set." >&2
  exit 2
fi

output_name="$1"
file="$2"

if [[ ! -f "$file" ]]; then
  echo "Output source file not found: ${file}" >&2
  exit 1
fi

delimiter="agent_output_${RANDOM}_${RANDOM}_$$"
while grep -Fxq "$delimiter" "$file"; do
  delimiter="agent_output_${RANDOM}_${RANDOM}_$$"
done

{
  echo "${output_name}<<${delimiter}"
  cat "$file"
  echo "${delimiter}"
} >> "$GITHUB_OUTPUT"
