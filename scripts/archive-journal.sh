#!/usr/bin/env bash
set -euo pipefail

mkdir -p agent/journal/archive
mapfile -t journal_files < <(find agent/journal -maxdepth 1 -type f -name '[0-9][0-9][0-9][0-9]-*.md' | sort)
active_count="${#journal_files[@]}"

if (( active_count <= 100 )); then
  echo "Active journal entries: ${active_count}; no archive move needed."
  exit 0
fi

archive_count=$((active_count - 100))
echo "Archiving ${archive_count} old journal entries."

for ((i = 0; i < archive_count; i++)); do
  mv "${journal_files[$i]}" agent/journal/archive/
done
