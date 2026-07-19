#!/usr/bin/env bash
set -euo pipefail

feedback_file="${1:-agent/shadow-feedback.md}"
baseline_file="${2:-target/agent-baseline-shadow.json}"
result_file="${REPEATED_CANDIDATE_RESULT_FILE:-${RUNNER_TEMP:-/tmp}/repeated-candidate-result.json}"

if [[ ! -f "$feedback_file" ]]; then
  echo "No previous rejection is awaiting comparison."
  exit 0
fi

previous_commit="$(sed -n 's/^- Commit: `\([0-9a-f]\{40\}\)`$/\1/p' "$feedback_file" | head -n 1)"
previous_branch="$(sed -n 's/^- Branch: `\(agent-rejected\/[a-zA-Z0-9._-]*\)`$/\1/p' "$feedback_file" | head -n 1)"
if [[ -z "$previous_commit" ]] || ! git cat-file -e "${previous_commit}^{commit}" 2>/dev/null; then
  echo "Previous feedback has no locally available preserved candidate commit."
  exit 0
fi

if [[ ! -f "$baseline_file" ]]; then
  echo "No current baseline is available; an identical diff will be re-evaluated rather than rejected as a repeat."
  exit 0
fi
previous_baseline_signature="$(awk '
  /^## Baseline Shadow Runs$/ { found = 1; next }
  found && /^```json$/ { json = 1; next }
  json && /^```$/ { exit }
  json { print }
' "$feedback_file" | jq -c '[.[] | {seed, requestedSteps, initial}]' 2>/dev/null || true)"
current_baseline_signature="$(jq -c '[.[] | {seed, requestedSteps, initial}]' "$baseline_file" 2>/dev/null || true)"
if [[ -z "$previous_baseline_signature" || "$previous_baseline_signature" != "$current_baseline_signature" ]]; then
  echo "The garden baseline changed since the previous rejection; an identical diff may be re-evaluated."
  exit 0
fi

candidate_paths=()
for candidate_path in src/main src/test pom.xml data/garden-state.txt; do
  if [[ -e "$candidate_path" ]] || [[ -n "$(git ls-files -- "$candidate_path")" ]]; then
    candidate_paths+=("$candidate_path")
  fi
done
if (( ${#candidate_paths[@]} == 0 )); then
  echo "No candidate source paths are available for comparison."
  exit 0
fi
temporary_index="$(mktemp)"
rm -f "$temporary_index"
trap 'rm -f "$temporary_index"' EXIT
GIT_INDEX_FILE="$temporary_index" git read-tree HEAD
GIT_INDEX_FILE="$temporary_index" git add -A -- "${candidate_paths[@]}"
current_patch_id="$(GIT_INDEX_FILE="$temporary_index" git diff --cached --binary HEAD -- "${candidate_paths[@]}" | git patch-id --stable | awk '{print $1}')"
previous_patch_id="$(git diff --binary "${previous_commit}^" "$previous_commit" -- "${candidate_paths[@]}" | git patch-id --stable | awk '{print $1}')"
if [[ -z "$current_patch_id" || "$current_patch_id" != "$previous_patch_id" ]]; then
  echo "Candidate differs from the previous rejected diff."
  exit 0
fi

jq -n \
  --arg branch "$previous_branch" \
  --arg commit "$previous_commit" \
  '{
    passed: false,
    policy: "repeat-guard",
    safetyPassed: true,
    targetPassed: false,
    metric: "previousRejectedDiff",
    goal: "change",
    requiredDelta: 1,
    baselineAverage: null,
    candidateAverage: null,
    observedDelta: 0,
    seeds: [],
    reason: "repeated-previous-rejection",
    previousRejectedBranch: $branch,
    previousRejectedCommit: $commit
  }' > "$result_file"
cat "$result_file"
echo "Candidate source diff and baseline context are identical to the previous rejected candidate ${previous_commit}." >&2
exit 1
