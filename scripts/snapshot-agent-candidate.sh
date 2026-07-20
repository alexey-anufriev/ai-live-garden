#!/usr/bin/env bash
set -euo pipefail

attempt="${1:-unknown}"
if ! [[ "$attempt" =~ ^[123]$ ]]; then
  echo "ATTEMPT must be 1, 2, or 3." >&2
  exit 2
fi
git config user.name "ai-live-garden[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
trap 'git restore --staged -- . >/dev/null 2>&1 || true' EXIT

git restore --staged -- .
candidate_paths=()
for candidate_path in src/main src/test pom.xml data/garden-state.txt; do
  if [[ -e "$candidate_path" ]] || [[ -n "$(git ls-files -- "$candidate_path")" ]]; then
    candidate_paths+=("$candidate_path")
  fi
done
if (( ${#candidate_paths[@]} == 0 )); then
  exit 1
fi
git add -A -- "${candidate_paths[@]}"
if git diff --cached --quiet; then
  exit 1
fi

candidate_tree="$(git write-tree)"
parent_commit="$(git rev-parse HEAD)"
candidate_commit="$({
  printf 'candidate: autonomous run %s attempt %s\n' "${GITHUB_RUN_ID:-local}" "$attempt"
} | git commit-tree "$candidate_tree" -p "$parent_commit")"
git restore --staged -- .
trap - EXIT
echo "$candidate_commit"
