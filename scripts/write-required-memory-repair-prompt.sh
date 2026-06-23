#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${GITHUB_OUTPUT:-}" ]]; then
  echo "GITHUB_OUTPUT is not set." >&2
  exit 2
fi

violations_file="${VALIDATE_REPAIR_VIOLATIONS_FILE:-${REQUIRED_MEMORY_VIOLATIONS_FILE:-}}"
if [[ -z "$violations_file" ]]; then
  echo "VALIDATE_REPAIR_VIOLATIONS_FILE or REQUIRED_MEMORY_VIOLATIONS_FILE is required." >&2
  exit 2
fi

attempt="${VALIDATE_REPAIR_ATTEMPT:-unknown}"
max_attempts="${VALIDATE_REPAIR_MAX_ATTEMPTS:-3}"
delimiter="REQUIRED_MEMORY_REPAIR_PROMPT_$(date +%s)_${RANDOM}"

{
  echo "text<<${delimiter}"
  echo "Repair missing required AI Live Garden memory updates."
  echo
  echo "This is repair attempt ${attempt} of ${max_attempts}."
  echo
  echo "The following required mutable memory files failed validation:"
  sed 's/^/- `/' "$violations_file" | sed 's/$/`/'
  echo
  echo "Required repair:"
  echo
  echo '1. Update `agent/state.md` by rewriting stale current-memory sections in place. Keep it compact and current; do not append a run log.'
  echo '2. Update only the `README.md` Current Garden State block between `<!-- AI-LIVE-GARDEN:STATE-START -->` and `<!-- AI-LIVE-GARDEN:STATE-END -->`.'
  echo '3. Keep the README health line shaped as `**Garden Health:** SYMBOL Status — one short reason.`'
  echo '4. Ground both files in the current repository state and `data/garden-state.txt`; do not claim absent organisms or effects as current activity.'
  echo '5. Do not modify protected files, workflows, archives, old journal entries, or unrelated source code.'
  echo '6. Run `scripts/validate-required-memory-updates.sh` before finishing.'
  echo
  echo "Useful sources:"
  echo
  echo '- `AGENTS.md` and `GEMINI.md` for authoritative memory rules.'
  echo '- `agent/state.md` and the current README state block.'
  echo '- `data/garden-state.txt` for the current garden snapshot.'
  echo '- `git diff --name-only HEAD` for the current run changes.'
  echo
  echo "Finish when required memory validation passes."
  echo "${delimiter}"
} >> "$GITHUB_OUTPUT"
