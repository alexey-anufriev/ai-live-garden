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
temporary_feedback="$(mktemp "${RUNNER_TEMP:-/tmp}/agent-feedback.XXXXXX")"
prior_feedback="$(mktemp "${RUNNER_TEMP:-/tmp}/prior-agent-feedback.XXXXXX")"
if [[ -f "$feedback_file" ]]; then
  cp "$feedback_file" "$prior_feedback"
fi
trap 'rm -f "$temporary_feedback" "$prior_feedback"' EXIT

{
  if [[ -s "$prior_feedback" ]]; then
    sed '/^## Subsequent Incomplete Attempt$/,$d' "$prior_feedback"
    echo
    echo "## Subsequent Incomplete Attempt"
  else
    echo "# Deferred Autonomous Run Feedback"
  fi
  echo
  echo "The previous agent call completed but did not leave both a valid handoff and a substantive implementation. No same-run agent retry was attempted. The incomplete source changes were discarded; use this evidence on the next autonomous run."
  echo
  echo "- Reason: ${reason}"
  echo
  echo "## Incomplete Change Paths"
  echo
  git status --short -uall || true
  echo
  echo "## Incomplete Change Summary"
  echo
  echo '```text'
  git diff --stat || true
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

# An incomplete call has no validated scope. Restore the entire checkout so the
# feedback-only commit cannot be blocked by an unrelated dirty path during its
# pull --rebase, and so no partial implementation leaks into a later run.
git restore --worktree --staged -- .
git clean -fd >/dev/null
mkdir -p "$feedback_dir"
mv "$temporary_feedback" "$feedback_file"
trap - EXIT
rm -f "$prior_feedback"

echo "Deferred incomplete autonomous run to ${feedback_file}."
