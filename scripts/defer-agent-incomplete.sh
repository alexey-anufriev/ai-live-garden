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
attempt_ledger_file="${AGENT_ATTEMPT_LEDGER_FILE:-}"

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
  prior_feedback_max_lines="${AGENT_PRIOR_FEEDBACK_MAX_LINES:-360}"
  if ! [[ "$prior_feedback_max_lines" =~ ^[1-9][0-9]*$ ]]; then
    echo "AGENT_PRIOR_FEEDBACK_MAX_LINES must be a positive integer." >&2
    exit 2
  fi
  sed -n "1,${prior_feedback_max_lines}p" "$feedback_file" > "$prior_feedback"
  if (( $(wc -l < "$feedback_file") > prior_feedback_max_lines )); then
    {
      echo
      echo "[Older feedback truncated after ${prior_feedback_max_lines} lines.]"
    } >> "$prior_feedback"
  fi
fi
trap 'rm -f "$temporary_feedback" "$prior_feedback"' EXIT

{
  echo "# Deferred Autonomous Run Feedback"
  echo
  echo "## Latest Incomplete Attempt"
  echo
  if [[ -n "$candidate_branch" && -n "$candidate_commit" ]]; then
    echo "The bounded autonomous attempt sequence left a substantive candidate but did not pass validation. The best candidate was preserved for assessment on the next run; it was removed from main and no garden tick occurred."
  else
    echo "The bounded autonomous attempt sequence did not leave both a valid handoff and a publishable substantive candidate. No garden tick occurred, and unvalidated worktree changes were removed from main."
  fi
  echo
  echo "- Reason: ${reason}"
  if [[ -n "$handoff_validation_reason" && "$handoff_validation_reason" != "none" ]]; then
    echo "- Handoff validation: ${handoff_validation_reason}"
  fi
  if [[ -f "$attempt_ledger_file" ]] && jq -e 'type == "array"' "$attempt_ledger_file" >/dev/null 2>&1; then
    echo "- Attempts completed: $(jq 'length' "$attempt_ledger_file") of 3"
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
  if [[ -n "$candidate_commit" ]] && git cat-file -e "${candidate_commit}^{commit}" 2>/dev/null; then
    git diff --name-status "${candidate_commit}^" "$candidate_commit" || true
  else
    git status --short -uall || true
  fi
  echo
  echo "## Incomplete Change Summary"
  echo
  echo '```text'
  if [[ -n "$candidate_commit" ]] && git cat-file -e "${candidate_commit}^{commit}" 2>/dev/null; then
    git diff --stat "${candidate_commit}^" "$candidate_commit" || true
  else
    git diff --stat || true
  fi
  echo '```'
  echo
  if [[ -n "$(git status --short -uall)" ]]; then
    echo "## Discarded Worktree Residue"
    echo
    echo '```text'
    git status --short -uall || true
    echo '```'
    echo
  fi
  if [[ -f "$attempt_ledger_file" ]] && jq -e 'type == "array"' "$attempt_ledger_file" >/dev/null 2>&1; then
    echo "## Bounded Attempt Results"
    echo
    echo "Each repair received the preceding deterministic failure and retained the same ecological objective and acceptance criteria. Zero-effect and wrong-direction results require a different causal mechanism. The preserved candidate is the substantive attempt that reached the highest validation stage."
    echo
    echo '```json'
    jq '[.[] | {attempt, accepted, acceptance, substantiveChange, candidateCommit, candidatePatchId, effectClassification, stage, reason, shadow}]' "$attempt_ledger_file"
    echo '```'
    echo
  fi
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
