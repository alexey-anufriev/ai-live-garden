#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${GITHUB_OUTPUT:-}" ]]; then
  echo "GITHUB_OUTPUT is not set." >&2
  exit 2
fi

violations_file="${VALIDATE_REPAIR_VIOLATIONS_FILE:-}"
if [[ -z "$violations_file" ]]; then
  echo "VALIDATE_REPAIR_VIOLATIONS_FILE is required." >&2
  exit 2
fi

attempt="${VALIDATE_REPAIR_ATTEMPT:-unknown}"
max_attempts="${VALIDATE_REPAIR_MAX_ATTEMPTS:-3}"
delimiter="TEST_REPAIR_PROMPT_$(date +%s)_${RANDOM}"

{
  echo "text<<${delimiter}"
  echo "Repair AI Live Garden Maven test failures."
  echo
  echo "This is repair attempt ${attempt} of ${max_attempts}."
  echo
  echo "The autonomous change has already been made, but \`mvn -B test\` failed."
  echo "Inspect the failing tests and the related production code, then make the smallest coherent repair."
  echo
  echo "Repair rules:"
  echo
  echo "1. Fix the root cause of the failing tests."
  echo "2. Prefer correcting production behavior when the test exposes a real regression."
  echo "3. Update a test expectation only when the production behavior is intentionally correct and the assertion is stale."
  echo "4. Do not delete, disable, or weaken tests just to make CI pass."
  echo "5. Do not modify protected files such as \`AGENTS.md\`, \`GEMINI.md\`, or \`.github/\`."
  echo "6. Keep the autonomous run's required journal, summary, state, and README updates intact."
  echo "7. Run \`mvn -B test\` before finishing."
  echo
  echo "Failure log excerpt:"
  echo
  echo '```text'
  tail -n 220 "$violations_file"
  echo '```'
  echo
  echo "Finish when \`mvn -B test\` passes."
  echo "${delimiter}"
} >> "$GITHUB_OUTPUT"
