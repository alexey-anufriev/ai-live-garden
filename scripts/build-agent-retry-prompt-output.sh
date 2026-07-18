#!/usr/bin/env bash
set -euo pipefail

if (( $# != 3 )); then
  echo "Usage: $0 ORIGINAL_CONTEXT GEMINI_ARTIFACT_DIR OUTPUT_CONTEXT" >&2
  exit 2
fi

original_context="$1"
artifact_dir="$2"
output_context="$3"
primary_artifact_dir="${AGENT_PRIMARY_GEMINI_ARTIFACTS_DIR:-${RUNNER_TEMP:-/tmp}/gemini-artifacts-primary}"
stdout_file="${artifact_dir}/stdout.log"

if [[ ! -f "$original_context" ]]; then
  echo "Original context file not found: ${original_context}" >&2
  exit 1
fi

if [[ ! -d "$artifact_dir" ]]; then
  echo "Gemini artifact directory not found: ${artifact_dir}" >&2
  exit 1
fi

mkdir -p "$primary_artifact_dir"
cp -R "${artifact_dir}/." "$primary_artifact_dir/"

previous_response_file="$(mktemp)"
trap 'rm -f "$previous_response_file"' EXIT

if [[ -f "$stdout_file" ]] && jq -e . "$stdout_file" >/dev/null 2>&1; then
  jq -r '.response // ""' "$stdout_file" > "$previous_response_file"
else
  : > "$previous_response_file"
fi

{
  echo "# AI Live Garden Corrective Agent Context"
  echo
  echo "The previous Gemini call completed, but it did not leave both a valid autonomous-run handoff and a substantive repository change."
  echo
  echo "This retry is already authorized. Do not enter plan mode. Do not ask for confirmation. Do not stop after proposing a strategy."
  echo
  echo "Required retry behavior:"
  echo
  if scripts/agent-substantive-changes.sh >/dev/null; then
    echo "- The previous call already changed the worktree. Inspect and preserve the focused existing change, correct it if needed, and finish its handoff. Do not start a second task."
  else
    echo "- The previous response did not leave a substantive implementation. Implement one focused bounded change under \`src/main/\`, \`src/test/\`, \`pom.xml\`, or, only for a demonstrated emergency, \`data/garden-state.txt\`."
  fi
  echo "- Leave real repository changes unless a required baseline repair makes that impossible."
  echo "- Write \`.agent-run.json\` before finishing."
  echo "- Repeat the exact same JSON object between \`AGENT_RUN_JSON_START\` and \`AGENT_RUN_JSON_END\` in the final response."
  echo "- Run \`scripts/run-maven-tests-with-timeout.sh\` if possible. Let it interrupt a runaway suite; a timeout must be diagnosed, not ignored."
  echo
  if [[ -s "$previous_response_file" ]]; then
    echo "## Previous Non-Executable Response"
    echo
    echo '```text'
    sed -n '1,120p' "$previous_response_file"
    echo '```'
    echo
  fi
  echo "## Original Autonomous Run Context"
  echo
  cat "$original_context"
} > "$output_context"

scripts/write-output-file.sh text "$output_context"
