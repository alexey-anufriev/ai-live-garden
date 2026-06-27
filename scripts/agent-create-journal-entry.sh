#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat >&2 <<'USAGE'
Usage:
  scripts/agent-create-journal-entry.sh --title TITLE --task TASK --reason REASON --checks CHECKS --test-result RESULT --observations OBSERVATIONS --next NEXT [--timestamp ISO_UTC]

Run this near the end of the agent step, after source, README, state, and
summary changes are already present in the worktree.
USAGE
  exit 2
}

title=""
task=""
reason=""
checks=""
test_result=""
observations=""
next_direction=""
timestamp="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

while (( $# > 0 )); do
  case "$1" in
    --title)
      title="${2:-}"
      shift 2
      ;;
    --task)
      task="${2:-}"
      shift 2
      ;;
    --reason)
      reason="${2:-}"
      shift 2
      ;;
    --checks)
      checks="${2:-}"
      shift 2
      ;;
    --test-result)
      test_result="${2:-}"
      shift 2
      ;;
    --observations)
      observations="${2:-}"
      shift 2
      ;;
    --next)
      next_direction="${2:-}"
      shift 2
      ;;
    --timestamp)
      timestamp="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      ;;
  esac
done

if [[ -z "$title" || -z "$task" || -z "$reason" || -z "$checks" || -z "$test_result" || -z "$observations" || -z "$next_direction" ]]; then
  usage
fi

if ! grep -Eq '^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z$' <<<"$timestamp"; then
  echo "Timestamp must be ISO-8601 UTC like YYYY-MM-DDTHH:MM:SSZ." >&2
  exit 2
fi

slug="$(
  tr '[:upper:]' '[:lower:]' <<<"$title" |
    sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//' |
    cut -c 1-60
)"
if [[ -z "$slug" ]]; then
  slug="agent-run"
fi

max_number="$(
  find agent/journal agent/journal/archive -type f -name '[0-9][0-9][0-9][0-9]-*.md' -printf '%f\n' 2>/dev/null |
    sed -E 's/^([0-9]{4})-.*/\1/' |
    sort -n |
    tail -n 1
)"
if [[ -z "$max_number" ]]; then
  next_number=0
else
  next_number=$((10#$max_number + 1))
fi

printf -v number "%04d" "$next_number"
path="agent/journal/${number}-${slug}.md"

mapfile -t changed_paths < <(
  {
    git diff --name-only -- .
    git ls-files --others --exclude-standard -- .
    printf '%s\n' "$path"
  } | sort -u
)

mkdir -p agent/journal
{
  echo "# ${title}"
  echo
  echo "## Timestamp"
  echo
  echo "$timestamp"
  echo
  echo "## Chosen task"
  echo
  echo "$task"
  echo
  echo "## Why this task was chosen"
  echo
  echo "$reason"
  echo
  echo "## Files changed"
  echo
  for changed_path in "${changed_paths[@]}"; do
    echo "- \`${changed_path}\`"
  done
  echo
  echo "## Checks run"
  echo
  echo "$checks"
  echo
  echo "## Result of \`mvn test\`"
  echo
  echo "$test_result"
  echo
  echo "## Observations"
  echo
  echo "$observations"
  echo
  echo "## Possible next directions"
  echo
  echo "- $next_direction"
} > "$path"

echo "$path"
