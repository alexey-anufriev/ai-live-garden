#!/usr/bin/env bash
set -euo pipefail

if (( $# > 1 )); then
  echo "Usage: $0 [BRANCH_TO_KEEP]" >&2
  exit 2
fi

keep_branch="${1:-}"
if [[ -n "$keep_branch" && ! "$keep_branch" =~ ^agent-rejected/[a-zA-Z0-9._-]+$ ]]; then
  echo "Kept branch must be under agent-rejected/: ${keep_branch}" >&2
  exit 2
fi

remote_refs="$(git ls-remote --heads origin 'refs/heads/agent-rejected/*')"
while read -r _ ref_name; do
  [[ -n "${ref_name:-}" ]] || continue
  branch_name="${ref_name#refs/heads/}"
  if [[ "$branch_name" == "$keep_branch" ]]; then
    continue
  fi
  git push origin --delete "$branch_name"
done <<< "$remote_refs"

if [[ -n "$keep_branch" ]]; then
  echo "Removed obsolete rejected candidate branches; kept ${keep_branch}."
else
  echo "Removed rejected candidate branches."
fi
