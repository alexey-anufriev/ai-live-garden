#!/usr/bin/env bash
set -euo pipefail

if (( $# < 2 || $# > 3 )); then
  echo "Usage: $0 EVALUATION_RESULT HANDOFF_FILE [FEEDBACK_FILE]" >&2
  exit 2
fi

evaluation_file="$1"
handoff_file="$2"
feedback_file="${3:-agent/shadow-feedback.md}"
baseline_shadow_file="${SHADOW_BASELINE_FILE:-}"
candidate_shadow_file="${SHADOW_CANDIDATE_FILE:-}"
rejected_branch="${REJECTED_CANDIDATE_BRANCH:-}"
rejected_commit="${REJECTED_CANDIDATE_COMMIT:-}"

if [[ ! "$rejected_branch" =~ ^agent-rejected/[a-zA-Z0-9._-]+$ ]]; then
  echo "REJECTED_CANDIDATE_BRANCH must identify a published agent-rejected branch." >&2
  exit 2
fi
if [[ ! "$rejected_commit" =~ ^[0-9a-f]{40}$ ]] || ! git cat-file -e "${rejected_commit}^{commit}" 2>/dev/null; then
  echo "REJECTED_CANDIDATE_COMMIT must identify the preserved candidate commit." >&2
  exit 2
fi

for required_file in "$evaluation_file" "$handoff_file"; do
  if [[ ! -f "$required_file" ]]; then
    echo "Deferred shadow feedback input not found: ${required_file}" >&2
    exit 1
  fi
done

scripts/validate-agent-handoff.sh "$handoff_file" >/dev/null
if ! jq -e '
  type == "object" and
  .passed == false and
  (.safetyPassed | type == "boolean") and
  (.targetPassed | type == "boolean") and
  (.metric | type == "string") and
  (.goal | type == "string") and
  ((.observedDelta | type == "number") or .observedDelta == null)
' "$evaluation_file" >/dev/null; then
  echo "Shadow rejection result is malformed or does not describe a rejected candidate." >&2
  exit 1
fi

feedback_dir="$(dirname "$feedback_file")"
mkdir -p "$feedback_dir"
temporary_feedback="$(mktemp "${feedback_dir}/.shadow-feedback.XXXXXX")"
trap 'rm -f "$temporary_feedback"' EXIT

{
  echo "# Deferred Shadow Evaluation Feedback"
  echo
  echo "This is machine-generated evidence from the immediately preceding rejected autonomous run. The rejected source changes were preserved on a dedicated branch, removed from main, and the garden was not advanced. The next agent must inspect this exact candidate and explicitly decide what to reuse, revise, or abandon before choosing its task. Repeating the same diff is not progress and will be rejected automatically."
  echo
  echo "## What Acceptance Required"
  echo
  echo "Baseline and candidate used the same committed garden state, seeds, and tick count. Acceptance compares their final metrics, not initial-to-final movement inside the candidate: \`observedDelta = candidateAverage - baselineAverage\`. An increase requires that delta to be at least \`requiredDelta\`; a decrease requires it to be at most negative \`requiredDelta\`; preserve requires its absolute value to remain within \`requiredDelta\`. Every run must also complete safely and stay within population bounds."
  echo
  echo "## Why This Candidate Was Rejected"
  echo
  jq -r '
    if .reason == "repeated-previous-rejection" then
      "The source diff and baseline context are identical to the supplied previous rejected candidate. No shadow run was needed because the ecological hypothesis was already disproved for this state and acceptance window."
    elif .safetyPassed == false then
      "Safety failed: the candidate shadow run did not complete safely, exceeded an operability bound, or removed a critical population present in the baseline."
    elif .targetPassed == false then
      "The declared ecological target was missed: baseline average \((.baselineAverage // "unavailable")), candidate average \((.candidateAverage // "unavailable")), observed delta \((.observedDelta // "unavailable")), required delta \(.requiredDelta) for goal \(.goal). Absolute movement within either simulation does not count as candidate impact."
    else
      "The deterministic shadow policy rejected the candidate; inspect the evaluation object below for the exact flags and reason."
    end
  ' "$evaluation_file"
  echo
  echo "## Rejected Candidate"
  echo
  echo "- Branch: \`${rejected_branch}\`"
  echo "- Commit: \`${rejected_commit}\`"
  echo "- Inspect: \`git show --stat ${rejected_commit}\`"
  echo "- Compare: \`git diff ${rejected_commit}^ ${rejected_commit}\`"
  echo
  echo "## Rejected Handoff"
  echo
  echo '```json'
  jq 'del(.codeMap)' "$handoff_file"
  echo '```'
  echo
  echo "## Deterministic Evaluation"
  echo
  echo '```json'
  jq . "$evaluation_file"
  echo '```'
  echo
  if [[ -n "$baseline_shadow_file" && -f "$baseline_shadow_file" ]] && jq -e . "$baseline_shadow_file" >/dev/null 2>&1; then
    echo "## Baseline Shadow Runs"
    echo
    echo '```json'
    jq '[.[] | {seed, requestedSteps, completedSteps, status, initial, final, minimumTotal, maximumTotal}]' "$baseline_shadow_file"
    echo '```'
    echo
  fi
  if [[ -n "$candidate_shadow_file" && -f "$candidate_shadow_file" ]] && jq -e . "$candidate_shadow_file" >/dev/null 2>&1; then
    echo "## Candidate Shadow Runs"
    echo
    echo '```json'
    jq '[.[] | {seed, requestedSteps, completedSteps, status, initial, final, minimumTotal, maximumTotal}]' "$candidate_shadow_file"
    echo '```'
    echo
  fi
} > "$temporary_feedback"

# A rejected ecological hypothesis is evidence, not source history. Keep the
# evidence and return the repository to the checked-out candidate baseline.
for candidate_path in src/main src/test pom.xml data/garden-state.txt; do
  if [[ -n "$(git ls-files -- "$candidate_path")" ]]; then
    git restore --worktree --staged -- "$candidate_path"
  fi
done
git clean -fd -- src/main src/test >/dev/null
rm -f "$handoff_file"
mv "$temporary_feedback" "$feedback_file"
trap - EXIT

echo "Deferred rejected shadow evaluation to ${feedback_file}."
