#!/usr/bin/env bash
set -euo pipefail

if (( $# < 2 || $# > 3 )); then
  echo "Usage: $0 GEMINI_ARTIFACT_DIR REASON [FEEDBACK_FILE]" >&2
  exit 2
fi

artifact_dir="$1"
reason="$2"
feedback_file="${3:-agent/shadow-feedback.md}"
stdout_file="${artifact_dir}/stdout.log"

feedback_dir="$(dirname "$feedback_file")"
mkdir -p "$feedback_dir"
temporary_feedback="$(mktemp "${feedback_dir}/.agent-feedback.XXXXXX")"
trap 'rm -f "$temporary_feedback"' EXIT

{
  echo "# Deferred Autonomous Run Feedback"
  echo
  echo "The previous agent call completed but did not leave both a valid handoff and a substantive implementation. No same-run agent retry was attempted. The incomplete source changes were discarded; use this evidence on the next autonomous run."
  echo
  echo "- Reason: ${reason}"
  echo
  echo "## Incomplete Change Paths"
  echo
  git status --short -uall -- src/main src/test pom.xml data/garden-state.txt || true
  echo
  echo "## Incomplete Change Summary"
  echo
  echo '```text'
  git diff --stat -- src/main src/test pom.xml data/garden-state.txt || true
  echo '```'
  echo
  if [[ -f "$stdout_file" ]] && jq -e . "$stdout_file" >/dev/null 2>&1; then
    echo "## Agent Output Summary"
    echo
    echo "- Tool calls: $(jq -r '.stats.tools.totalCalls // 0' "$stdout_file")"
    echo "- Plan-mode calls: $(jq -r '.stats.tools.byName.enter_plan_mode.count // 0' "$stdout_file")"
    response_file="$(mktemp)"
    jq -r '.response // empty' "$stdout_file" > "$response_file"
    if [[ -s "$response_file" ]]; then
      echo
      echo '```text'
      sed -n '1,160p' "$response_file"
      echo '```'
    fi
    rm -f "$response_file"
  fi
} > "$temporary_feedback"

for candidate_path in src/main src/test pom.xml data/garden-state.txt; do
  if [[ -n "$(git ls-files -- "$candidate_path")" ]]; then
    git restore --worktree --staged -- "$candidate_path"
  fi
done
git clean -fd -- src/main src/test >/dev/null
rm -f .agent-run.json
mv "$temporary_feedback" "$feedback_file"
trap - EXIT

echo "Deferred incomplete autonomous run to ${feedback_file}."
