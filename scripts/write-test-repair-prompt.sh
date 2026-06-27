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

emit_section_from_command() {
  local title="$1"
  shift

  echo "$title"
  echo
  echo '```text'
  "$@" || true
  echo '```'
  echo
}

emit_limited_diff() {
  local title="$1"
  shift

  echo "$title"
  echo
  echo '```diff'
  git diff -- "$@" | sed -n '1,260p'
  echo '```'
  echo
}

emit_untracked_source_files() {
  local listed=false

  while IFS= read -r path; do
    case "$path" in
      src/main/java/*|src/test/java/*|pom.xml)
        if [[ "$listed" == false ]]; then
          echo "Untracked source or test files:"
          echo
          listed=true
        fi
        echo "### $path"
        echo
        echo '```text'
        sed -n '1,220p' "$path"
        echo '```'
        echo
        ;;
    esac
  done < <(git ls-files --others --exclude-standard)

  if [[ "$listed" == false ]]; then
    echo "Untracked source or test files: none"
    echo
  fi
}

{
  echo "text<<${delimiter}"
  echo "Repair AI Live Garden Maven test failures."
  echo
  echo "This is repair attempt ${attempt} of ${max_attempts}."
  echo
  echo "The autonomous change has already been made, but \`mvn -B test\` failed."
  echo "Inspect the failing tests and the related production code, then make the narrowest coherent repair."
  echo "Use the generated diff context below to understand what the autonomous step changed."
  echo
  echo "Repair rules:"
  echo
  echo "1. Fix the root cause of the failing tests."
  echo "2. Prefer correcting production behavior when the test exposes a real regression."
  echo "3. Update a test expectation only when the production behavior is intentionally correct and the assertion is stale."
  echo "4. Do not delete, disable, or weaken tests just to make CI pass."
  echo "5. Do not modify protected files such as \`AGENTS.md\`, \`GEMINI.md\`, or \`.github/\`."
  echo "6. Do not edit generated memory files: \`README.md\`, \`agent/state.md\`, \`agent/requests.md\`, \`agent/code-map.md\`, \`agent/journal/\`, \`agent/summaries/\`, or \`agent/templates/\`."
  echo "7. Keep \`.agent-run.json\` valid; update it only if the repair changes the run summary, limitations, expected garden effect, next direction, requests, or code-map descriptions."
  echo "8. Run \`mvn -B test\` before finishing."
  echo
  emit_section_from_command "Changed files:" git status --short
  emit_section_from_command "Tracked diff summary:" git diff --stat
  emit_section_from_command "Tracked diff name/status:" git diff --name-status
  emit_limited_diff "Focused Java and test diff, first 260 lines:" src/main/java src/test/java pom.xml
  emit_untracked_source_files
  echo "Failure log excerpt:"
  echo
  echo '```text'
  tail -n 220 "$violations_file"
  echo '```'
  echo
  echo "Finish when \`mvn -B test\` passes."
  echo "${delimiter}"
} >> "$GITHUB_OUTPUT"
