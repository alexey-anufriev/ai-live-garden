#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${GITHUB_OUTPUT:-}" ]]; then
  echo "GITHUB_OUTPUT is not set." >&2
  exit 2
fi

violation_file="${VALIDATE_REPAIR_VIOLATIONS_FILE:-${JOURNAL_FORMAT_VIOLATIONS_FILE:-}}"
if [[ -z "$violation_file" ]]; then
  echo "VALIDATE_REPAIR_VIOLATIONS_FILE or JOURNAL_FORMAT_VIOLATIONS_FILE is required." >&2
  exit 2
fi

attempt="${VALIDATE_REPAIR_ATTEMPT:-unknown}"
max_attempts="${VALIDATE_REPAIR_MAX_ATTEMPTS:-3}"
delimiter="JOURNAL_REPAIR_PROMPT_$(date +%s)_${RANDOM}"

{
  echo "text<<${delimiter}"
  echo "Repair malformed AI Live Garden journal entries."
  echo
  echo "This is repair attempt ${attempt} of ${max_attempts}."
  echo
  echo "The following active journal files or journal conditions failed validation:"
  sed 's/^/- `/' "$violation_file" | sed 's/$/`/'
  echo
  echo "Required repair:"
  echo
  echo '1. Use `agent/templates/journal-entry.md` as the authoritative structure.'
  echo '2. If no changed active journal entry exists, create the missing numbered journal entry under `agent/journal/`.'
  echo "3. Keep the exact heading order from the template."
  echo '4. Replace every `{{PLACEHOLDER}}` value with concrete text.'
  echo "5. Preserve the historical meaning of the malformed entry; do not invent unrelated work."
  echo '6. Use an ISO-8601 UTC timestamp with seconds, such as `YYYY-MM-DDTHH:MM:SSZ`.'
  echo "7. Do not edit unrelated journal files."
  echo "8. Do not modify protected files."
  echo '9. Run `VALIDATE_REPAIR_VIOLATIONS_FILE=/tmp/journal-format-violations.txt scripts/validate-journal-format.sh` before finishing.'
  echo "10. The journal must describe the final state of the current run, not a stale intermediate state."
  echo '11. If post-change test validation succeeded, the journal test-result section must start with `Passed` or `Success`.'
  echo '12. The `## Files changed` section must list every currently changed path from `git diff --name-only HEAD` and `git ls-files --others --exclude-standard`, excluding `agent/journal/archive/`.'
  echo '13. Include the new journal file itself, required memory files, summaries, `data/garden-state.txt`, source/test files, and other changed files as separate backtick-quoted bullet items.'
  echo
  echo "Useful sources:"
  echo
  echo '- The malformed journal file itself.'
  echo '- `git diff --name-only HEAD` for files changed in the current run.'
  echo '- `agent/last-run.md` for workflow test outcomes when available.'
  echo
  echo "Finish when journal format validation passes."
  echo "${delimiter}"
} >> "$GITHUB_OUTPUT"
