#!/usr/bin/env bash
set -euo pipefail

if (( $# != 3 )); then
  echo "Usage: $0 RAW_CONTEXT_FILE COMPACTED_CONTEXT_FILE OUTPUT_FILE" >&2
  exit 2
fi

raw_context_file="$1"
compacted_context_file="$2"
output_file="$3"
metadata_file="${EVOLVE_CONTEXT_METADATA_FILE:-${output_file}.metadata}"

if [[ ! -f "$raw_context_file" ]]; then
  echo "Raw context file not found: $raw_context_file" >&2
  exit 2
fi

if [[ ! -s "$compacted_context_file" ]]; then
  echo "Compacted context file is missing or empty: $compacted_context_file" >&2
  exit 2
fi

mkdir -p "$(dirname "$output_file")"
mkdir -p "$(dirname "$metadata_file")"

copy_before_heading() {
  local stop_heading="$1"

  awk -v stop="$stop_heading" '
    $0 == stop { exit }
    { print }
  ' "$raw_context_file"
}

copy_section() {
  local start_heading="$1"
  local stop_heading="$2"

  awk -v start="$start_heading" -v stop="$stop_heading" '
    $0 == start { printing=1 }
    printing && $0 == stop { exit }
    printing { print }
  ' "$raw_context_file"
}

{
  copy_before_heading "## Current Agent Memory"
  echo "## Compacted Continuity Context"
  echo
  sed -n '1,$p' "$compacted_context_file"
  echo
  copy_section "## Persistent Garden State Digest" "## Recent Active Journal Entries"
  copy_section "## Project Index" "__END_OF_CONTEXT__"
} > "$output_file"

context_line_count="$(wc -l < "$output_file" | tr -d '[:space:]')"
context_byte_count="$(wc -c < "$output_file" | tr -d '[:space:]')"
compacted_line_count="$(wc -l < "$compacted_context_file" | tr -d '[:space:]')"
compacted_byte_count="$(wc -c < "$compacted_context_file" | tr -d '[:space:]')"

{
  echo "EVOLVE_CONTEXT_COMPACTED_FILE=${output_file}"
  echo "EVOLVE_CONTEXT_COMPACTED_LINES=${context_line_count}"
  echo "EVOLVE_CONTEXT_COMPACTED_BYTES=${context_byte_count}"
  echo "EVOLVE_CONTEXT_COMPACTION_DIGEST_LINES=${compacted_line_count}"
  echo "EVOLVE_CONTEXT_COMPACTION_DIGEST_BYTES=${compacted_byte_count}"
} >> "$metadata_file"
