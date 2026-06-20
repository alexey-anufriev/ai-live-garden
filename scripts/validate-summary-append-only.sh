#!/usr/bin/env bash
set -euo pipefail

is_active_summary_path() {
  local path="$1"

  case "$path" in
    agent/summaries/daily/archive/*|\
      agent/summaries/weekly/archive/*|\
      agent/summaries/monthly/archive/*|\
      agent/summaries/yearly/archive/*)
      return 1
      ;;
    agent/summaries/daily/*.md|\
      agent/summaries/weekly/*.md|\
      agent/summaries/monthly/*.md|\
      agent/summaries/yearly/*.md)
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

failed=0
violation_file="${SUMMARY_APPEND_ONLY_VIOLATIONS_FILE:-}"

if [[ -n "$violation_file" ]]; then
  : > "$violation_file"
fi

record_violation() {
  local path="$1"

  failed=1
  if [[ -n "$violation_file" ]]; then
    printf '%s\n' "$path" >> "$violation_file"
  fi
}

while IFS= read -r path; do
  if is_active_summary_path "$path"; then
    echo "Summary append-only violation: ${path} was deleted."
    record_violation "$path"
  fi
done < <(git diff --name-only --diff-filter=D -- agent/summaries)

while IFS= read -r path; do
  if ! is_active_summary_path "$path"; then
    continue
  fi

  old_size="$(git cat-file -s "HEAD:${path}")"
  new_size="$(wc -c < "$path")"

  if (( new_size < old_size )); then
    echo "Summary append-only violation: ${path} is shorter than the committed version."
    record_violation "$path"
    continue
  fi

  old_copy="$(mktemp)"
  git show "HEAD:${path}" > "$old_copy"

  if ! cmp -n "$old_size" "$old_copy" "$path" >/dev/null; then
    echo "Summary append-only violation: ${path} changed existing content instead of only appending."
    record_violation "$path"
  fi

  rm -f "$old_copy"
done < <(git diff --name-only --diff-filter=M -- agent/summaries)

if (( failed != 0 )); then
  echo "Existing active summaries are append-only. Add a new entry at the end instead of editing prior text."
  exit 1
fi

echo "Active summary changes are append-only."
