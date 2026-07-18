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
  echo "This is machine-generated evidence from the previous autonomous run. The rejected source changes were discarded, the garden was not advanced, and no same-run agent retry was attempted. Treat this evidence as input: do not repeat the rejected hypothesis without addressing why its declared effect was absent."
  echo
  echo "## Rejected Handoff"
  echo
  echo '```json'
  jq . "$handoff_file"
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
    jq . "$baseline_shadow_file"
    echo '```'
    echo
  fi
  if [[ -n "$candidate_shadow_file" && -f "$candidate_shadow_file" ]] && jq -e . "$candidate_shadow_file" >/dev/null 2>&1; then
    echo "## Candidate Shadow Runs"
    echo
    echo '```json'
    jq . "$candidate_shadow_file"
    echo '```'
    echo
  fi
  echo "## Rejected Change Paths"
  echo
  git status --short -uall -- src/main src/test pom.xml data/garden-state.txt || true
  echo
  echo "## Rejected Change Summary"
  echo
  echo '```text'
  git diff --stat -- src/main src/test pom.xml data/garden-state.txt || true
  echo '```'
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
