#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

output_file="$1"
mkdir -p "$(dirname "$output_file")"

tmp_result="$(mktemp)"
trap 'rm -f "$tmp_result"' EXIT

scope="${VALIDATE_AGENT_WORKTREE_SCOPE:-baseline}"

set +e
VALIDATE_AGENT_WORKTREE_SCOPE="$scope" \
  VALIDATE_AGENT_WORKTREE_VIOLATIONS_FILE="$tmp_result" \
  scripts/validate-agent-worktree.sh
status="$?"
set -e

case "$status" in
  0)
    result="passed"
    instruction="The committed project started this run with no worktree policy violations in scope."
    ;;
  10)
    result="deferred-repair"
    instruction="The committed project started this run with repairable worktree policy violations. The autonomous agent must repair these before choosing unrelated work."
    ;;
  *)
    result="hard-failure"
    instruction="The committed project started this run with hard worktree policy violations. Human or harness repair may be required before a safe autonomous commit."
    ;;
esac

{
  echo "- Command: \`scripts/validate-agent-worktree.sh\`"
  echo "- Scope: \`${scope}\`"
  echo "- Exit code: ${status}"
  echo "- Result: ${result}"
  echo
  echo "$instruction"
  echo
  echo "## Output"
  echo
  echo '```text'
  sed -n '1,$p' "$tmp_result"
  echo '```'
} > "$output_file"

exit "$status"
