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

{
  echo "${output_name}<<EOF"
  cat "$file"
  echo "EOF"
} >> "$GITHUB_OUTPUT"
