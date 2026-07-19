#!/usr/bin/env bash
set -euo pipefail

declare -A kept_branches=()
for keep_branch in "$@"; do
  if [[ ! "$keep_branch" =~ ^agent-rejected/[a-zA-Z0-9._-]+$ ]]; then
    echo "Kept branch must be under agent-rejected/: ${keep_branch}" >&2
    exit 2
  fi
  kept_branches["$keep_branch"]=1
done

remote_refs="$(git ls-remote --heads origin 'refs/heads/agent-rejected/*')"
while read -r _ ref_name; do
  [[ -n "${ref_name:-}" ]] || continue
  branch_name="${ref_name#refs/heads/}"
  if [[ -n "${kept_branches[$branch_name]:-}" ]]; then
    continue
  fi
  git push origin --delete "$branch_name"
done <<< "$remote_refs"

if (( ${#kept_branches[@]} > 0 )); then
  echo "Removed obsolete rejected candidate branches; kept ${!kept_branches[*]}."
else
  echo "Removed rejected candidate branches."
fi
