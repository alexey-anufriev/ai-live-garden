#!/usr/bin/env bash
set -euo pipefail

violation_file="${VALIDATE_REPAIR_VIOLATIONS_FILE:-${SUMMARY_FORMAT_VIOLATIONS_FILE:-}}"

if [[ -n "$violation_file" ]]; then
  : > "$violation_file"
fi

record_violation() {
  local path="$1"
  local message="$2"

  echo "Summary format violation in ${path}: ${message}"
  if [[ -n "$violation_file" ]]; then
    printf '%s\n' "$path" >> "$violation_file"
  fi
}

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

mapfile -t changed_summary_files < <(
  {
    git diff --name-only --diff-filter=AM -- agent/summaries
    git ls-files --others --exclude-standard -- agent/summaries
  } | sort -u |
    while IFS= read -r path; do
      if is_active_summary_path "$path"; then
        printf '%s\n' "$path"
      fi
    done
)

if (( ${#changed_summary_files[@]} == 0 )); then
  echo "No changed active summary files to format-check."
  exit 0
fi

failed=0

for path in "${changed_summary_files[@]}"; do
  if grep -q '{{' "$path"; then
    record_violation "$path" "unresolved template placeholder"
    failed=1
  fi

  if ! awk '
    /^### / {
      if (NR > 1 && previous != "") {
        printf "heading at line %d must be preceded by a blank line\n", NR
        bad = 1
      }
      heading_line = NR
      next
    }
    heading_line && NR == heading_line + 1 {
      if ($0 != "") {
        printf "heading at line %d must be followed by a blank line\n", heading_line
        bad = 1
      }
    }
    { previous = $0 }
    END { exit bad }
  ' "$path"; then
    record_violation "$path" "entries must have one blank line before each ### heading and one blank line after each ### heading"
    failed=1
  fi
done

if (( failed != 0 )); then
  echo "Changed active summary entries must match the template spacing."
  exit 1
fi

echo "Changed active summary entries match template spacing."
