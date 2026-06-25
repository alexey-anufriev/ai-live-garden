#!/usr/bin/env bash
set -euo pipefail

if (( $# != 2 )); then
  echo "Usage: $0 RAW_CONTEXT_FILE OUTPUT_FILE" >&2
  exit 2
fi

raw_context_file="$1"
output_file="$2"
recent_journal_limit="${AGENT_CONTEXT_RECENT_JOURNAL_LIMIT:-8}"

if [[ ! -f "$raw_context_file" ]]; then
  echo "Raw context file not found: $raw_context_file" >&2
  exit 2
fi

mkdir -p "$(dirname "$output_file")"

copy_section() {
  local start_heading="$1"
  local stop_heading="$2"

  awk -v start="$start_heading" -v stop="$stop_heading" '
    $0 == start { printing=1 }
    printing && $0 == stop { exit }
    printing { print }
  ' "$raw_context_file"
}

copy_continuity_source() {
  copy_section "## Current Agent Memory" "## Persistent Garden State Digest" | awk '
    /^- Cycle:/ { next }
    /^- Nutrients:/ { next }
    /^- NutrientBuffer:/ { next }
    /^- \*\*Cycle:\*\*/ { next }
    /^- \*\*Status:\*\* Nutrients:/ { next }
    { print }
  '
}

latest_files() {
  local limit="$1"
  shift
  find "$@" -maxdepth 1 -type f ! -name ".gitkeep" -print 2>/dev/null | sort -V | tail -n "$limit"
}

append_journal_digest() {
  local journal="$1"

  echo "### ${journal}"
  awk '
    NR == 1 {
      sub(/^# /, "", $0)
      print "- Title: " $0
    }
    /^## Timestamp$/ { section="timestamp"; next }
    /^## Chosen task$/ { section="task"; next }
    /^## Files changed$/ { section="files"; print "- Files changed:"; next }
    /^## Result of `mvn test`$/ { section="test"; next }
    /^## Observations$/ { section="observations"; next }
    /^## / { section=""; next }
    section == "timestamp" && $0 != "" { print "- Timestamp: " $0; next }
    section == "task" && $0 != "" { print "- Task: " $0; next }
    section == "files" && /^- / { print "  " $0; next }
    section == "test" && $0 != "" { print "- Test result: " $0; next }
    section == "observations" && $0 != "" { print "- Observations: " $0; next }
  ' "$journal"
  echo
}

{
  echo "# AI Live Garden Context Compaction"
  echo
  echo "Condense the context below for a later autonomous coding agent."
  echo
  echo "Your output will be inserted into the final prompt as the compacted continuity context. It must preserve useful continuity while spending far fewer tokens than the source."
  echo
  echo "The final prompt separately includes exact run rules, authoritative templates, the current garden-state digest, and the project file index. Do not restate those unless they appear in the continuity source as a current constraint or human preference."
  echo
  echo "The source memory may contain stale numeric garden-state facts. Do not carry exact cycle numbers, population counts, nutrient values, or other snapshot metrics into the digest. Preserve qualitative pressures and continuity only; the final prompt will append the authoritative current garden-state digest."
  echo
  echo "Preserve:"
  echo
  echo "- qualitative garden pressures and visible themes mentioned in memory or recent history;"
  echo "- current project memory, open requests, and active constraints;"
  echo "- recent implemented changes and failure patterns;"
  echo "- human preferences that affect future autonomous runs;"
  echo "- exact file paths that matter;"
  echo "- warnings about append-only summaries, journal format, protected files, and CI constraints only when they are active recent concerns."
  echo
  echo "Do not:"
  echo
  echo "- choose the next task;"
  echo "- invent requirements, facts, organisms, tests, or files;"
  echo "- rewrite repository rules as new policy;"
  echo "- turn a repeated recent implementation pattern into a recommendation to keep repeating it;"
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
  echo "--- SOURCE CONTEXT START ---"
  copy_continuity_source
  echo "## Recent Active Journal Entries Digest"
  echo
  while IFS= read -r journal; do
    append_journal_digest "$journal"
  done < <(latest_files "$recent_journal_limit" "agent/journal")
  echo "--- SOURCE CONTEXT END ---"
} > "$output_file"
