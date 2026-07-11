#!/usr/bin/env bash
set -euo pipefail

limit="${GARDEN_OUTCOME_HISTORY_LIMIT:-12}"

if ! [[ "$limit" =~ ^[1-9][0-9]*$ ]]; then
  echo "GARDEN_OUTCOME_HISTORY_LIMIT must be a positive integer." >&2
  exit 2
fi

snapshot() {
  local label="$1"
  local ref="${2:-}"

  if [[ -n "$ref" ]]; then
    git show "${ref}:data/garden-state.txt" 2>/dev/null
  else
    sed -n '1,$p' data/garden-state.txt
  fi | awk -F'[=|]' -v label="$label" '
    /^cycle=/ { cycle = $2 }
    /^organism=/ { count[$3]++ }
    END {
      printf "%s\t%s\t%d\t%d\t%d\t%d\t%d\t%d\n",
        label, cycle, count["FOX"], count["FUNGUS"], count["ROOT_NETWORK"],
        count["BEETLE"], count["MOSS"], count["SPORE"]
    }
  '
}

autonomous_commits() {
  git log --grep='^feat: autonomous garden evolution' --format='%H' -n "$limit" 2>/dev/null || true
}

journal_title() {
  local commit="$1"
  local journal

  journal="$(git show --name-only --format='' "$commit" 2>/dev/null |
    awk '/^agent\/journal\/[0-9].*[.]md$/ { print; exit }')"
  if [[ -n "$journal" ]]; then
    git show "${commit}:${journal}" 2>/dev/null | sed -n '1s/^# //p'
  else
    git show -s --format='%s' "$commit"
  fi
}

commits=()
rows=()
mapfile -t commits < <(autonomous_commits)
rows+=("$(snapshot worktree)")
for commit in "${commits[@]}"; do
  rows+=("$(snapshot "${commit:0:7}" "$commit")")
done

echo "## Ecological Outcome History"
echo
echo "Machine-computed from the current state and the last ${#commits[@]} autonomous coding commits. Use this to distinguish implemented code from achieved garden outcomes."
echo
echo "| Snapshot | Cycle | Fox | Fungus | Root network | Beetle | Moss | Spore |"
echo "| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |"
printf '%s\n' "${rows[@]}" | awk -F'\t' '{ printf "| %s | %s | %s | %s | %s | %s | %s | %s |\n", $1, $2, $3, $4, $5, $6, $7, $8 }'
echo

if (( ${#rows[@]} >= 4 )); then
  if printf '%s\n' "${rows[@]}" | awk -F'\t' '
    NR == 1 { fox = $3; fungus = $4; root = $5 }
    $3 != fox || $4 != fungus || $5 != root { changed = 1 }
    END { exit changed ? 1 : 0 }
  '; then
    echo "> Stagnation signal: fox, fungus, and root-network counts did not change across this entire coding-run window. Another equivalent trait, threshold, or efficiency tweak is not evidence of progress; diagnose the active current-state bottleneck before changing behavior again."
    echo
  fi
fi

echo "### Recent Autonomous Hypotheses"
echo
if (( ${#commits[@]} == 0 )); then
  echo "- No autonomous coding commits found."
else
  for commit in "${commits[@]}"; do
    printf -- '- `%s`: %s\n' "${commit:0:7}" "$(journal_title "$commit")"
  done
fi
