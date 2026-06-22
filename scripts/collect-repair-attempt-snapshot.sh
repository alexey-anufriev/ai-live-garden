#!/usr/bin/env bash
set -euo pipefail

output_dir="${1:-}"
attempt="${2:-}"

if [[ -z "$output_dir" || -z "$attempt" ]]; then
  echo "Usage: $0 OUTPUT_DIR ATTEMPT" >&2
  exit 2
fi

mkdir -p "$output_dir"

git status --short > "$output_dir/git-status-short.txt"
git diff --stat > "$output_dir/git-diff-stat.txt"
git diff --binary > "$output_dir/git-diff.patch"
git diff --name-status > "$output_dir/git-diff-name-status.txt"
git ls-files --others --exclude-standard > "$output_dir/git-untracked-files.txt"

if [[ -s "$output_dir/git-untracked-files.txt" ]]; then
  mkdir -p "$output_dir/untracked-files"
  while IFS= read -r path; do
    if [[ -f "$path" ]]; then
      mkdir -p "$output_dir/untracked-files/$(dirname "$path")"
      cp "$path" "$output_dir/untracked-files/$path"
    fi
  done < "$output_dir/git-untracked-files.txt"
fi

{
  echo "# Repair Attempt ${attempt} Snapshot"
  echo
  echo "Generated at: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo
  echo "This snapshot was captured before Gemini repair attempt ${attempt}."
  echo
  echo "## Changed files"
  echo
  if [[ -s "$output_dir/git-diff-name-status.txt" ]]; then
    sed 's/^/- /' "$output_dir/git-diff-name-status.txt"
  else
    echo "- No tracked file changes"
  fi
  echo
  echo "## Untracked files"
  echo
  if [[ -s "$output_dir/git-untracked-files.txt" ]]; then
    sed 's/^/- /' "$output_dir/git-untracked-files.txt"
  else
    echo "- None"
  fi
} > "$output_dir/README.md"
