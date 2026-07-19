#!/usr/bin/env bash
set -euo pipefail

violations=()

add_violation() {
  violations+=("$1")
}

while IFS= read -r path; do
  [[ -n "$path" ]] || continue
  case "$path" in
    agent/plans/[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].md|agent/plans/[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].json|agent/plans/.gitkeep)
      ;;
    *)
      add_violation "Project Manager workflow may only change dated agent/plans/YYYY-MM-DD.md and .json files: ${path}"
      ;;
  esac
done < <(
  {
    git diff --name-only --diff-filter=ACMR -- .
    git ls-files --others --exclude-standard -- .
  } | sort -u
)

if ! find agent/plans -maxdepth 1 -type f -name '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].md' -print -quit | grep -q .; then
  add_violation "Missing dated project plan under agent/plans/YYYY-MM-DD.md"
fi

while IFS= read -r plan; do
  [[ -n "$plan" ]] || continue
  [[ -f "${plan%.md}.json" ]] || add_violation "Missing machine-readable sidecar for ${plan}"
done < <(
  {
    git diff --name-only --diff-filter=ACMR -- 'agent/plans/*.md'
    git ls-files --others --exclude-standard -- 'agent/plans/*.md'
  } | sort -u
)

if (( ${#violations[@]} > 0 )); then
  {
    echo "Project Manager worktree validation failed:"
    printf -- '- %s\n' "${violations[@]}"
  } >&2
  exit 1
fi

echo "Project Manager worktree contains only rendered plan files."
