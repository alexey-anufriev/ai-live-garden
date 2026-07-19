#!/usr/bin/env bash
set -euo pipefail

if (( $# < 1 || $# > 2 )); then
  echo "Usage: $0 TRAIT [STATE_FILE]" >&2
  exit 2
fi

trait="$1"
state_file="${2:-data/garden-state.txt}"
if [[ ! "$trait" =~ ^[a-z0-9-]+$ ]]; then
  echo "Trait must use lowercase letters, digits, and hyphens: ${trait}" >&2
  exit 2
fi
if [[ ! -f "$state_file" ]]; then
  echo "Garden state not found: ${state_file}" >&2
  exit 1
fi

awk -F '|' -v wanted="$trait" '
  /^organism=/ {
    traits = $6
    gsub(/\\,/, ",", traits)
    count = split(traits, values, ",")
    for (i = 1; i <= count; i++) {
      if (values[i] == wanted) {
        carriers++
        break
      }
    }
  }
  END { print carriers + 0 }
' "$state_file"
