#!/usr/bin/env bash
set -euo pipefail

state_file="${1:-data/garden-state.txt}"
if [[ ! -f "$state_file" ]]; then
  echo "⚫|Dormant|the persistent garden state is unavailable."
  exit 0
fi

count_type() {
  local type="$1"
  local source="$2"
  awk -F'[=|]' -v type="$type" '$1 == "organism" && $3 == type { count++ } END { print count + 0 }' "$source"
}

total="$(awk -F'[=|]' '$1 == "organism" { count++ } END { print count + 0 }' "$state_file")"
nutrients="$(awk -F= '$1 == "nutrients" { print int($2); exit }' "$state_file")"
buffer="$(awk -F= '$1 == "nutrientBuffer" { print int($2); exit }' "$state_file")"
fungus="$(count_type FUNGUS "$state_file")"
beetle="$(count_type BEETLE "$state_file")"
hare="$(count_type HARE "$state_file")"
fox="$(count_type FOX "$state_file")"
herbivores=$((beetle + hare))

if (( total == 0 )); then
  echo "🔴|Critical|no organisms remain in the persistent state."
  exit 0
fi
if (( fungus == 0 || herbivores == 0 || fox == 0 )); then
  echo "🔴|Critical|one or more essential decomposer, herbivore, or predator roles are extinct."
  exit 0
fi
if (( fungus < 3 || herbivores < 3 || fox < 3 )); then
  echo "🟠|Strained|an essential ecological role is below three viable organisms."
  exit 0
fi
if (( total > 25000 )); then
  echo "🟠|Strained|the population exceeds the bounded operating envelope of 25000 organisms."
  exit 0
fi
if (( nutrients <= 0 && buffer <= 0 )); then
  echo "🟠|Strained|both immediately available nutrients and the nutrient buffer are exhausted."
  exit 0
fi

largest="$(awk -F'[=|]' '$1 == "organism" { count[$3]++ } END { for (type in count) if (count[type] > max) max=count[type]; print max + 0 }' "$state_file")"
if (( largest * 100 >= total * 70 )); then
  echo "🟠|Strained|one organism type holds at least 70 percent of the living population."
  exit 0
fi

if [[ "$state_file" == "data/garden-state.txt" ]] && git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  reference_commit="$(git log --format='%H' -n 20 -- data/garden-state.txt | tail -n 1)"
  if [[ -n "$reference_commit" ]]; then
    if git cat-file -e "${reference_commit}:data/garden-state.txt" 2>/dev/null; then
      for type in FUNGUS BEETLE FOX; do
        current="$(count_type "$type" "$state_file")"
        previous="$(git show "${reference_commit}:data/garden-state.txt" | awk -F'[=|]' -v type="$type" '$1 == "organism" && $3 == type { count++ } END { print count + 0 }')"
        if (( previous >= 10 && current * 2 < previous )); then
          echo "🟠|Strained|the ${type,,} population fell by more than half across the recent observation window."
          exit 0
        fi
      done
    fi
  fi
fi

if (( nutrients > 10 && buffer > 10 && fungus >= 10 && herbivores >= 10 && fox >= 10 )); then
  echo "🟢|Flourishing|all essential roles are viable without recent collapse, dominance, or resource exhaustion."
else
  echo "🟡|Stable|the garden remains viable but one or more roles or resources have limited resilience."
fi
