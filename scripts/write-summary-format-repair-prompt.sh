#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${GITHUB_OUTPUT:-}" ]]; then
  echo "GITHUB_OUTPUT is not set." >&2
  exit 2
fi

violation_file="${VALIDATE_REPAIR_VIOLATIONS_FILE:-${SUMMARY_FORMAT_VIOLATIONS_FILE:-}}"
if [[ -z "$violation_file" ]]; then
  echo "VALIDATE_REPAIR_VIOLATIONS_FILE or SUMMARY_FORMAT_VIOLATIONS_FILE is required." >&2
  exit 2
fi

attempt="${VALIDATE_REPAIR_ATTEMPT:-unknown}"
max_attempts="${VALIDATE_REPAIR_MAX_ATTEMPTS:-3}"
delimiter="SUMMARY_FORMAT_REPAIR_PROMPT_$(date +%s)_${RANDOM}"

{
  echo "text<<${delimiter}"
  echo "Repair AI Live Garden summary entry spacing."
  echo
  echo "This is repair attempt ${attempt} of ${max_attempts}."
  echo
  echo "The following active summary files failed format validation:"
  sed 's/^/- `/' "$violation_file" | sed 's/$/`/'
  echo
  echo "Required repair:"
  echo
  echo '1. Match the spacing in the matching summary template under `agent/templates/`.'
  echo '2. Each `### TIMESTAMP - Title` entry heading must be preceded by a blank line.'
  echo '3. Each `### TIMESTAMP - Title` entry heading must be followed by a blank line before its paragraph.'
  echo '4. Do not leave `{{PLACEHOLDER}}` text.'
  echo "5. Preserve the existing meaning of each summary entry."
  echo "6. Do not modify protected files."
  echo '7. Run `scripts/validate-summary-format.sh` before finishing.'
  echo
  echo "Finish when summary format validation passes."
  echo "${delimiter}"
} >> "$GITHUB_OUTPUT"
