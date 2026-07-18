#!/usr/bin/env bash
set -euo pipefail

if (( $# != 3 )); then
  echo "Usage: $0 ORIGINAL_CONTEXT SHADOW_EVALUATION_RESULT OUTPUT_CONTEXT" >&2
  exit 2
fi

original_context="$1"
evaluation_result="$2"
output_context="$3"

for required_file in "$original_context" "$evaluation_result" .agent-run.json; do
  if [[ ! -f "$required_file" ]]; then
    echo "Shadow corrective retry input not found: ${required_file}" >&2
    exit 1
  fi
done

mkdir -p "$(dirname "$output_context")"
{
  echo "# AI Live Garden Shadow Corrective Context"
  echo
  echo "The first implementation passed tests and safety checks but missed its declared deterministic ecological target. This is the one authorized shadow corrective attempt."
  echo
  echo "Required behavior:"
  echo "- Keep the same focused task and inspect the existing worktree; do not start a second feature."
  echo "- Diagnose why the current living state and five-step seeds did not exercise the intended behavior."
  echo "- Revise the implementation so its intended ecological consequence is observable, or replace the evaluation metric only when another metric truthfully captures that same consequence."
  echo "- Do not weaken the target merely to pass and do not use a preserve goal for behavior-changing work."
  echo "- Update \`.agent-run.json\`, run the bounded Maven tests, and run the exact shadow preflight from the original context before finishing."
  echo "- Repeat the final JSON between \`AGENT_RUN_JSON_START\` and \`AGENT_RUN_JSON_END\`."
  echo
  echo "## Rejected Shadow Evaluation"
  echo
  echo '```json'
  jq . "$evaluation_result"
  echo '```'
  echo
  echo "## Current Handoff"
  echo
  echo '```json'
  jq . .agent-run.json
  echo '```'
  echo
  echo "## Current Change Summary"
  echo
  echo '```text'
  git status --short
  git diff --stat
  echo '```'
  echo
  echo "## Original Autonomous Context"
  echo
  cat "$original_context"
} > "$output_context"

scripts/write-output-file.sh text "$output_context"
