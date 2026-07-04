#!/usr/bin/env bash
set -euo pipefail

violations=()

add_violation() {
  violations+=("$1")
}

while IFS= read -r path; do
  [[ -n "$path" ]] || continue
  case "$path" in
    agent/plans/*.md|agent/plans/.gitkeep)
      ;;
    *)
      add_violation "Project Manager workflow may only change agent/plans files: ${path}"
      ;;
  esac
done < <(
  {
    git diff --name-only --diff-filter=ACMR -- .
    git ls-files --others --exclude-standard -- .
  } | sort -u
)

if [[ ! -f agent/plans/latest.md ]]; then
  add_violation "Missing rendered latest project plan: agent/plans/latest.md"
fi

if ! find agent/plans -maxdepth 1 -type f -name '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].md' -print -quit | grep -q .; then
  add_violation "Missing dated project plan under agent/plans/YYYY-MM-DD.md"
fi

if (( ${#violations[@]} > 0 )); then
  {
    echo "Project Manager worktree validation failed:"
    printf -- '- %s\n' "${violations[@]}"
  } >&2
  exit 1
fi

echo "Project Manager worktree contains only rendered plan files."
