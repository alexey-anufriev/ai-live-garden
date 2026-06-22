#!/usr/bin/env bash
set -euo pipefail

output_file="${1:-}"
if [[ -z "$output_file" ]]; then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

mkdir -p "$(dirname "$output_file")"

status_lines="$(git status --porcelain)"
changed_files="$(printf '%s\n' "$status_lines" | sed '/^$/d' | sed 's/^...//; s/.* -> //')"
changed_count="$(printf '%s\n' "$changed_files" | sed '/^$/d' | wc -l | tr -d '[:space:]')"

changed_under() {
  local prefix="$1"
  printf '%s\n' "$changed_files" | awk -v prefix="$prefix" 'index($0, prefix) == 1 { found=1 } END { print found ? "yes" : "no" }'
}

changed_exact() {
  local path="$1"
  printf '%s\n' "$changed_files" | awk -v path="$path" '$0 == path { found=1 } END { print found ? "yes" : "no" }'
}

{
  echo "## Change Diagnostics"
  echo
  echo "- Changed files before commit, including untracked: ${changed_count}"
  echo "- Updated \`agent/state.md\`: $(changed_exact "agent/state.md")"
  echo "- Updated README current-state block candidate: $(changed_exact "README.md")"
  echo "- Added or changed active journal: $(changed_under "agent/journal/")"
  echo "- Added or changed summaries: $(changed_under "agent/summaries/")"
  echo "- Changed persistent garden state: $(changed_exact "data/garden-state.txt")"
  echo "- Changed Java source: $(changed_under "src/main/java/")"
  echo "- Changed Java tests: $(changed_under "src/test/java/")"
  echo
  echo "### Changed Files"
  echo
  if [[ -n "$status_lines" ]]; then
    printf '%s\n' "$status_lines" | sed 's/^/- /'
  else
    echo "- No file changes"
  fi
  echo
  echo "### Tracked Diff Stat"
  echo
  if ! git diff --quiet; then
    echo '```text'
    git diff --stat
    echo '```'
  else
    echo "- No tracked diff"
  fi
} > "$output_file"
