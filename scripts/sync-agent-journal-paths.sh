#!/usr/bin/env bash
set -euo pipefail

changed_paths() {
  {
    git diff --name-only --diff-filter=ACMR -- .
    git ls-files --others --exclude-standard -- .
  } | sort -u |
    while IFS= read -r path; do
      case "$path" in
        agent/journal/archive/*|agent/last-run.md|.agent-run.json|target/*)
          ;;
        *)
          printf '%s\n' "$path"
          ;;
      esac
    done
}

mapfile -t journals < <(
  {
    git diff --name-only --diff-filter=AM -- agent/journal
    git ls-files --others --exclude-standard -- agent/journal
  } | sort -u | awk '/^agent\/journal\/[0-9][0-9][0-9][0-9]-.*[.]md$/'
)

if (( ${#journals[@]} != 1 )); then
  echo "Expected exactly one generated active journal entry, found ${#journals[@]}." >&2
  exit 1
fi

mapfile -t paths < <(changed_paths)
journal="${journals[0]}"
temporary="$(mktemp "${RUNNER_TEMP:-/tmp}/agent-journal.XXXXXX")"
trap 'rm -f "$temporary"' EXIT

awk -v paths="$(printf '%s\034' "${paths[@]}")" '
  BEGIN {
    count = split(paths, changed, "\034")
  }
  $0 == "## Files changed" {
    print
    print ""
    for (i = 1; i <= count; i++) {
      if (changed[i] != "") print "- `" changed[i] "`"
    }
    replacing = 1
    next
  }
  replacing && /^## / {
    replacing = 0
    print ""
    print
    next
  }
  !replacing {
    print
  }
' "$journal" > "$temporary"

mv "$temporary" "$journal"
trap - EXIT
echo "Synchronized ${journal} with the final changed-path set."
