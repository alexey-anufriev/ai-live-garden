#!/usr/bin/env bash
set -euo pipefail

violation_file="${VALIDATE_REPAIR_VIOLATIONS_FILE:-${JOURNAL_FORMAT_VIOLATIONS_FILE:-}}"

if [[ -n "$violation_file" ]]; then
  : > "$violation_file"
fi

record_violation() {
  local path="$1"
  local message="$2"

  echo "Journal format violation in ${path}: ${message}"
  if [[ -n "$violation_file" ]]; then
    printf '%s\t%s\n' "$path" "$message" >> "$violation_file"
  fi
}

section_text() {
  local path="$1"
  local heading="$2"

  awk -v heading="$heading" '
    $0 == heading {
      in_section = 1
      next
    }
    in_section && /^## / {
      exit
    }
    in_section {
      print
    }
  ' "$path"
}

first_non_empty_line() {
  awk 'NF { print; exit }'
}

changed_paths_for_journal() {
  {
    git diff --name-only --diff-filter=ACMR -- .
    git ls-files --others --exclude-standard -- .
  } | sort -u |
    while IFS= read -r path; do
      case "$path" in
        agent/journal/archive/*|agent/last-run.md)
          ;;
        *)
          printf '%s\n' "$path"
          ;;
      esac
    done
}

is_active_journal_path() {
  local path="$1"

  case "$path" in
    agent/journal/archive/*)
      return 1
      ;;
    agent/journal/*.md)
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

mapfile -t changed_journal_files < <(
  {
    git diff --name-only --diff-filter=AM -- agent/journal
    git ls-files --others --exclude-standard -- agent/journal
  } | sort -u |
    while IFS= read -r path; do
      if is_active_journal_path "$path"; then
        printf '%s\n' "$path"
      fi
    done
)

mapfile -t changed_paths < <(changed_paths_for_journal)

if (( ${#changed_journal_files[@]} == 0 )); then
  echo "Journal format violation: no added or modified active journal entry found."
  if [[ -n "$violation_file" ]]; then
    printf '%s\n' "NO_CHANGED_ACTIVE_JOURNAL_ENTRY" >> "$violation_file"
  fi
  exit 1
fi

required_headings=(
  "## Timestamp"
  "## Chosen task"
  "## Why this task was chosen"
  "## Files changed"
  "## Checks run"
  "## Result of \`mvn test\`"
  "## Observations"
  "## Possible next directions"
)

failed=0

for path in "${changed_journal_files[@]}"; do
  if grep -q '{{' "$path"; then
    record_violation "$path" "unresolved template placeholder"
    failed=1
  fi

  if ! head -n 1 "$path" | grep -Eq '^# [^{}]+$'; then
    record_violation "$path" "first line must be a filled H1 title"
    failed=1
  fi

  previous_line=0
  for heading in "${required_headings[@]}"; do
    line="$(grep -nFx "$heading" "$path" | head -n 1 | cut -d: -f1 || true)"
    if [[ -z "$line" ]]; then
      record_violation "$path" "missing heading '${heading}'"
      failed=1
      continue
    fi
    if (( line <= previous_line )); then
      record_violation "$path" "heading '${heading}' is out of order"
      failed=1
    fi
    previous_line="$line"
  done

  timestamp_line="$(awk '/^## Timestamp$/ { getline; while ($0 == "") getline; print; exit }' "$path")"
  if ! grep -Eq '^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z$' <<<"$timestamp_line"; then
    record_violation "$path" "timestamp must be ISO-8601 UTC like YYYY-MM-DDTHH:MM:SSZ"
    failed=1
  fi

  if [[ "${JOURNAL_MVN_TEST_OUTCOME:-}" == "success" ]]; then
    result_line="$(section_text "$path" "## Result of \`mvn test\`" | first_non_empty_line)"
    if ! grep -Eq '^[[:space:]]*([Pp]ass(ed)?|[Ss]uccess)([[:space:][:punct:]]|$)' <<<"$result_line"; then
      record_violation "$path" "mvn test result must describe the final successful post-change test validation, not an intermediate or stale failure"
      failed=1
    fi
  fi

  files_changed_section="$(section_text "$path" "## Files changed")"
  for changed_path in "${changed_paths[@]}"; do
    if ! grep -Fq -- "\`${changed_path}\`" <<<"$files_changed_section"; then
      record_violation "$path" "Files changed section must include \`${changed_path}\`"
      failed=1
    fi
  done
done

if (( failed != 0 )); then
  echo "Changed active journal entries must follow agent/templates/journal-entry.md exactly."
  exit 1
fi

echo "Changed active journal entries match the journal template."
