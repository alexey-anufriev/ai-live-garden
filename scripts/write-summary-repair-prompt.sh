#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${GITHUB_OUTPUT:-}" ]]; then
  echo "GITHUB_OUTPUT is not set." >&2
  exit 2
fi

if [[ -z "${SUMMARY_APPEND_ONLY_VIOLATIONS_FILE:-}" ]]; then
  echo "SUMMARY_APPEND_ONLY_VIOLATIONS_FILE is required." >&2
  exit 2
fi

attempt="${SUMMARY_REPAIR_ATTEMPT:-${VALIDATE_REPAIR_ATTEMPT:-unknown}}"
max_attempts="${SUMMARY_REPAIR_MAX_ATTEMPTS:-${VALIDATE_REPAIR_MAX_ATTEMPTS:-3}}"
delimiter="SUMMARY_REPAIR_PROMPT_$(date +%s)_${RANDOM}"

{
  echo "text<<${delimiter}"
  echo "Repair summary append-only violations for AI Live Garden."
  echo
  echo "This is repair attempt ${attempt} of ${max_attempts}."
  echo
  echo "The following active summary files failed append-only validation:"
  sed 's/^/- `/' "$SUMMARY_APPEND_ONLY_VIOLATIONS_FILE" | sed 's/$/`/'
  echo
  echo "Required repair:"
  echo
  echo "1. For each violating file, restore the committed content as the exact prefix of the file."
  echo "2. Preserve the current run's intended summary information only by appending a new timestamped entry after the final committed line."
  echo "3. Do not edit, rewrap, reorder, shorten, normalize, or replace any committed summary text."
  echo "4. Do not modify unrelated files."
  echo "5. Do not modify protected files."
  echo '6. Run `VALIDATE_REPAIR_VIOLATIONS_FILE=/tmp/summary-append-only-violations.txt scripts/validate-summary-append-only.sh` before finishing.'
  echo
  echo "Useful commands:"
  echo
  echo '- View committed summary content with `git show HEAD:path/to/summary.md`.'
  echo '- View current changes with `git diff -- path/to/summary.md`.'
  echo '- The final repaired file must begin byte-for-byte with the committed `HEAD` version.'
  echo
  echo "Finish when active summary validation passes."
  echo "${delimiter}"
} >> "$GITHUB_OUTPUT"
