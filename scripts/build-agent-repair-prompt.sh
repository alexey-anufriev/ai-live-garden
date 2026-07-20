#!/usr/bin/env bash
set -euo pipefail

if (( $# != 5 )); then
  echo "Usage: $0 ATTEMPT ORIGINAL_CONTEXT PRIOR_ARTIFACT_DIR ATTEMPT_RESULT OUTPUT_CONTEXT" >&2
  exit 2
fi

attempt="$1"
if ! [[ "$attempt" =~ ^[23]$ ]]; then
  echo "Repair ATTEMPT must be 2 or 3." >&2
  exit 2
fi
original_context="$2"
artifact_dir="$3"
attempt_result="$4"
output_context="$5"
previous_attempt=$(( attempt - 1 ))
archive_dir="${RUNNER_TEMP:-/tmp}/gemini-artifacts-attempt-${previous_attempt}"

for required_file in "$original_context" "$attempt_result"; do
  if [[ ! -f "$required_file" ]]; then
    echo "Repair prompt input not found: ${required_file}" >&2
    exit 1
  fi
done
if [[ ! -d "$artifact_dir" ]]; then
  echo "Gemini artifact directory not found: ${artifact_dir}" >&2
  exit 1
fi

rm -rf "$archive_dir"
mkdir -p "$archive_dir" "$(dirname "$output_context")"
cp -R "$artifact_dir/." "$archive_dir/"

previous_response="$(mktemp)"
trap 'rm -f "$previous_response"' EXIT
if [[ -f "$artifact_dir/stdout.log" ]] && jq -e . "$artifact_dir/stdout.log" >/dev/null 2>&1; then
  jq -r '.response // empty' "$artifact_dir/stdout.log" > "$previous_response"
else
  : > "$previous_response"
fi

{
  echo "# AI Live Garden Bounded Repair Attempt ${attempt} of 3"
  echo
  echo "The previous attempt did not satisfy the executable candidate contract. Repair it now using the exact evidence below. This is not a new planning run."
  echo
  echo "Required behavior:"
  echo "- Keep the same PM direction and ecological objective. Do not restart broad repository exploration."
  echo "- Inspect the existing worktree first. Repair the current implementation when the failure is local and actionable."
  echo "- If the evidence proves the mechanism unsafe or unreachable, replace it with a smaller causal mechanism for the same objective and record the prior mechanism as abandoned."
  echo "- Do not revert to a clean no-op. Leave the best substantive candidate that you can validate."
  echo "- Run focused tests before the full bounded Maven suite. For evolution, declare a truthful evaluation target; the harness will independently measure and write the final differential evidence."
  echo "- Update .agent-run.json and repeat it between AGENT_RUN_JSON_START and AGENT_RUN_JSON_END."
  echo "- You have this attempt and at most one later repair attempt."
  echo
  echo "## Exact Previous Attempt Result"
  echo
  echo '```json'
  jq . "$attempt_result"
  echo '```'
  echo
  echo "## Current Candidate"
  echo
  echo '```text'
  git status --short -uall
  git diff --stat
  echo '```'
  if [[ -f .agent-run.json ]]; then
    echo
    echo "### Current Handoff"
    echo
    echo '```json'
    jq . .agent-run.json 2>/dev/null || sed -n '1,200p' .agent-run.json
    echo '```'
  fi
  if [[ -s "$previous_response" ]]; then
    echo
    echo "## Previous Agent Response"
    echo
    echo '```text'
    sed -n '1,160p' "$previous_response"
    echo '```'
  fi
  echo
  echo "## Original Autonomous Context"
  echo
  cat "$original_context"
} > "$output_context"

scripts/write-output-file.sh text "$output_context"
