#!/usr/bin/env bash
set -euo pipefail

if (( $# != 2 )); then
  echo "Usage: $0 RAW_CONTEXT_FILE OUTPUT_FILE" >&2
  exit 2
fi

raw_context_file="$1"
output_file="$2"

if [[ ! -f "$raw_context_file" ]]; then
  echo "Raw context file not found: $raw_context_file" >&2
  exit 2
fi

mkdir -p "$(dirname "$output_file")"

{
  echo "# AI Live Garden Context Compaction"
  echo
  echo "Condense the context below for a later autonomous coding agent."
  echo
  echo "Your output will be inserted into the final prompt as the compacted continuity context. It must preserve useful continuity while spending far fewer tokens than the source."
  echo
  echo "Preserve:"
  echo
  echo "- current garden state facts and visible pressures;"
  echo "- current project memory, open requests, and active constraints;"
  echo "- recent implemented changes and failure patterns;"
  echo "- human preferences that affect future autonomous runs;"
  echo "- exact file paths that matter;"
  echo "- warnings about append-only summaries, journal format, protected files, and CI constraints."
  echo
  echo "Do not:"
  echo
  echo "- choose the next task;"
  echo "- invent requirements, facts, organisms, tests, or files;"
  echo "- rewrite repository rules as new policy;"
  echo "- omit uncertainty when the source is unclear;"
  echo "- include markdown fences around the whole response."
  echo
  echo "Output concise Markdown under these headings:"
  echo
  echo "## Compacted Current Memory"
  echo "## Compacted Recent Evolution"
  echo "## Active Constraints And Human Preferences"
  echo "## Relevant Files And Checks"
  echo
  echo "Source context:"
  echo
  echo '```markdown'
  sed -n '1,$p' "$raw_context_file"
  echo '```'
} > "$output_file"
