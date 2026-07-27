#!/usr/bin/env bash
set -euo pipefail

state_file="${1:-${AGENT_GARDEN_STATE_FILE:-data/garden-state.txt}}"

if [[ ! -f "$state_file" ]]; then
  echo "Garden state file not found: ${state_file}" >&2
  exit 1
fi

awk -F'[=|]' '
  /^cycle=/ { cycle = $2 + 0 }
  /^light=/ { light = $2 + 0 }
  /^moisture=/ { moisture = $2 + 0 }
  /^warmth=/ { warmth = $2 + 0 }
  /^nutrients=/ { nutrients = $2 + 0 }
  /^nutrientBuffer=/ { nutrientBuffer = $2 + 0 }
  /^organism=/ { total++; populations[$3]++ }
  END {
    printf "{\"cycle\":%d,\"environment\":{\"light\":%d,\"moisture\":%d,\"warmth\":%d,\"nutrients\":%d,\"nutrientBuffer\":%d},\"totalOrganisms\":%d,\"populations\":{", cycle, light, moisture, warmth, nutrients, nutrientBuffer, total
    first = 1
    for (type in populations) {
      if (!first) printf ","
      first = 0
      printf "\"%s\":%d", type, populations[type]
    }
    print "}}"
  }
' "$state_file"
