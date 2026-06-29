#!/usr/bin/env bash
set -euo pipefail

state_value() {
  local key="$1"
  awk -F= -v key="$key" '$1 == key { print $2; exit }' data/garden-state.txt
}

organism_counts() {
  awk -F'[=|]' '/^organism=/ { count[$3]++; total++ } END {
    printf "total=%d\n", total
    for (type in count) printf "%s=%d\n", type, count[type]
  }' data/garden-state.txt | sort
}

display_label() {
  local value="$1"
  tr '[:upper:]_' '[:lower:] ' <<<"$value" |
    awk '{
      for (i = 1; i <= NF; i++) {
        $i = toupper(substr($i, 1, 1)) substr($i, 2)
      }
      print
    }'
}

missing_roles_csv() {
  local counts="$1"
  local missing=()

  grep -q '^FUNGUS=' <<<"$counts" || missing+=("fungus")
  if ! grep -Eq '^(BEETLE|HARE)=' <<<"$counts"; then
    missing+=("herbivores")
  fi
  grep -q '^FOX=' <<<"$counts" || missing+=("predators")

  if (( ${#missing[@]} == 0 )); then
    echo "none"
  else
    printf '%s\n' "${missing[@]}" | paste -sd ', ' -
  fi
}

health_status() {
  local total="$1"
  local nutrients="$2"
  local missing="$3"

  if (( total == 0 )); then
    echo "🔴|Critical|no organisms remain in the persistent state."
  elif [[ "$missing" != "none" && "${nutrients%.*}" -le 0 ]]; then
    echo "🟠|Strained|available nutrients are exhausted while ecological roles are missing."
  elif [[ "$missing" != "none" ]]; then
    echo "🟡|Stable|the garden persists but ecological roles are still missing."
  elif (( ${nutrients%.*} > 10 )); then
    echo "🟢|Flourishing|diverse roles persist with usable nutrients."
  else
    echo "🟡|Stable|diverse roles persist under limited nutrients."
  fi
}

readme_state_body() {
  local cycle="$1"
  local total="$2"
  local missing="$3"

  {
    echo "At cycle ${cycle}, the garden has this committed shape:"
    echo
    echo "### Organisms"
    echo
    echo "- Total: ${total}"
    echo "- Breakdown:"
    organism_counts |
      awk -F= '$1 != "total" { print $1 "\t" $2 }' |
      sort |
      while IFS=$'\t' read -r type count; do
        echo "  - $(display_label "$type"): ${count}"
      done
    echo
    echo "![Organism trends](agent/organism-trends.svg)"
    echo
    echo "### Garden Characteristics"
    echo
    awk -F= '
      /^#/ { next }
      /^$/ { exit }
      /^[^=]+=.*$/ {
        label = $1
        if (label == "cycle") label = "Cycle"
        else if (label == "version") label = "Version"
        else if (label == "nextId") label = "Next id"
        else if (label == "light") label = "Light"
        else if (label == "moisture") label = "Moisture"
        else if (label == "warmth") label = "Warmth"
        else if (label == "nutrients") label = "Nutrients"
        else if (label == "nutrientBuffer") label = "Nutrient buffer"
        else {
          gsub(/_/, " ", label)
          label = toupper(substr(label, 1, 1)) substr(label, 2)
        }
        printf "- %s: %s\n", label, $2
      }
    ' data/garden-state.txt
    echo "- Missing roles: ${missing}"
    echo
    echo "![Garden trends](agent/garden-trends.svg)"
  }
}

scripts/generate-garden-trends-svg.sh agent/garden-trends.svg agent/organism-trends.svg

cycle="$(state_value cycle)"
nutrients="$(state_value nutrients)"
counts="$(organism_counts)"
total="$(awk -F= '$1 == "total" { print $2 }' <<<"$counts")"
missing="$(missing_roles_csv "$counts")"
health_record="$(health_status "${total:-0}" "${nutrients:-0}" "$missing")"
IFS='|' read -r health_symbol health_label health_reason <<<"$health_record"
readme_narrative="$(readme_state_body "$cycle" "${total:-0}" "$missing")"

scripts/agent-update-readme-state.sh \
  --symbol "$health_symbol" \
  --status "$health_label" \
  --reason "$health_reason" \
  --narrative "$readme_narrative"
