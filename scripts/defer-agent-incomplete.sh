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
candidate_branch="${INCOMPLETE_CANDIDATE_BRANCH:-}"
candidate_commit="${INCOMPLETE_CANDIDATE_COMMIT:-}"
handoff_validation_reason="${HANDOFF_VALIDATION_REASON:-}"

if [[ -n "$candidate_branch" && ! "$candidate_branch" =~ ^agent-rejected/[a-zA-Z0-9._-]+$ ]]; then
  echo "INCOMPLETE_CANDIDATE_BRANCH must identify an agent-rejected branch." >&2
  exit 2
fi
if [[ -n "$candidate_commit" && ! "$candidate_commit" =~ ^[0-9a-f]{40}$ ]]; then
  echo "INCOMPLETE_CANDIDATE_COMMIT must identify a candidate commit." >&2
  exit 2
fi

feedback_dir="$(dirname "$feedback_file")"
mkdir -p "$feedback_dir"
temporary_feedback="$(mktemp "${RUNNER_TEMP:-/tmp}/agent-feedback.XXXXXX")"
prior_feedback="$(mktemp "${RUNNER_TEMP:-/tmp}/prior-agent-feedback.XXXXXX")"
if [[ -f "$feedback_file" ]]; then
  if grep -Fq '## Prior Feedback' "$feedback_file"; then
    sed -n '/^## Prior Feedback$/,$p' "$feedback_file" | sed '1d' > "$prior_feedback"
  elif grep -Fq '## Subsequent Incomplete Attempt' "$feedback_file"; then
    sed '/^## Subsequent Incomplete Attempt$/,$d' "$feedback_file" > "$prior_feedback"
  else
    cp "$feedback_file" "$prior_feedback"
  fi
fi
trap 'rm -f "$temporary_feedback" "$prior_feedback"' EXIT

{
  echo "# Deferred Autonomous Run Feedback"
  echo
  echo "## Latest Incomplete Attempt"
  echo
  if [[ -n "$candidate_branch" && -n "$candidate_commit" ]]; then
    echo "The agent call left a substantive candidate but not a valid handoff. Its exact source was preserved for assessment on the next run; it was removed from main and no garden tick occurred."
  else
    echo "The agent call did not leave both a valid handoff and a publishable substantive candidate. No same-run retry was attempted, no garden tick occurred, and unvalidated worktree changes were removed from main."
  fi
  echo
  echo "- Reason: ${reason}"
  if [[ -n "$handoff_validation_reason" && "$handoff_validation_reason" != "none" ]]; then
    echo "- Handoff validation: ${handoff_validation_reason}"
  fi
  if [[ -n "$candidate_branch" && -n "$candidate_commit" ]]; then
    echo
    echo "## Preserved Incomplete Candidate"
    echo
    echo "- Branch: \`${candidate_branch}\`"
    echo "- Commit: \`${candidate_commit}\`"
    echo "- Inspect: \`git show --stat ${candidate_commit}\`"
    echo "- Compare: \`git diff ${candidate_commit}^ ${candidate_commit}\`"
  fi
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
  if [[ -s "$prior_feedback" ]]; then
    echo
    echo "## Prior Feedback"
    echo
    awk '
      { lines[NR] = $0 }
      END {
        last = NR
        while (last > 0 && lines[last] ~ /^[[:space:]]*$/) last--
        for (line = 1; line <= last; line++) print lines[line]
      }
    ' "$prior_feedback"
  fi
} > "$temporary_feedback"

# The publish step preserves any substantive candidate before this point.
# Restore the checkout so only deterministic feedback reaches main and no
# unvalidated partial implementation leaks into the next run's worktree.
git restore --worktree --staged -- .
git clean -fd >/dev/null
mkdir -p "$feedback_dir"
mv "$temporary_feedback" "$feedback_file"
trap - EXIT
rm -f "$prior_feedback"

echo "Deferred incomplete autonomous run to ${feedback_file}."
