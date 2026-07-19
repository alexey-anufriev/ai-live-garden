#!/usr/bin/env bash
set -euo pipefail

if (( $# > 1 )); then
  echo "Usage: $0 [BRANCH_NAME]" >&2
  exit 2
fi

run_id="${GITHUB_RUN_ID:-local-$(date -u +%Y%m%dT%H%M%SZ)}"
run_attempt="${GITHUB_RUN_ATTEMPT:-1}"
branch_name="${1:-agent-rejected/${run_id}-${run_attempt}}"

if [[ ! "$branch_name" =~ ^agent-rejected/[a-zA-Z0-9._-]+$ ]]; then
  echo "Rejected candidate branch must be under agent-rejected/: ${branch_name}" >&2
  exit 2
fi

git config user.name "ai-live-garden[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
trap 'git restore --staged -- . >/dev/null 2>&1 || true' EXIT

# Worktree policy has already constrained the candidate to these paths. Build a
# commit directly from the index so main and the working tree never move.
git restore --staged -- .
candidate_paths=()
for candidate_path in src/main src/test pom.xml data/garden-state.txt; do
  if [[ -e "$candidate_path" ]] || [[ -n "$(git ls-files -- "$candidate_path")" ]]; then
    candidate_paths+=("$candidate_path")
  fi
done
if (( ${#candidate_paths[@]} == 0 )); then
  echo "No candidate source paths exist in this checkout." >&2
  exit 1
fi
git add -A -- "${candidate_paths[@]}"
if git diff --cached --quiet; then
  git restore --staged -- .
  echo "No rejected candidate source changes are available to publish." >&2
  exit 1
fi

candidate_tree="$(git write-tree)"
parent_commit="$(git rev-parse HEAD)"
candidate_commit="$(
  printf 'rejected: preserve autonomous candidate from run %s attempt %s\n' "$run_id" "$run_attempt" |
    git commit-tree "$candidate_tree" -p "$parent_commit"
)"
git restore --staged -- .
trap - EXIT

git push origin "${candidate_commit}:refs/heads/${branch_name}"

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "branch=${branch_name}"
    echo "commit=${candidate_commit}"
  } >> "$GITHUB_OUTPUT"
fi

echo "Published rejected candidate ${candidate_commit} to ${branch_name}."
