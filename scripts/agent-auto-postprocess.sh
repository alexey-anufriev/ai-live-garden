#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat >&2 <<'USAGE'
Usage:
  scripts/agent-auto-postprocess.sh --test-outcome OUTCOME [--checks TEXT] [--handoff-file PATH]

Restores generated memory files after the agent step, then regenerates README
state, agent/state.md, agent/code-map.md, daily summary, scheduled rollups,
requests, and journal from the current repository diff, data/garden-state.txt,
and agent handoff JSON.
USAGE
  exit 2
}

test_outcome=""
checks="mvn -B test"
timestamp="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
handoff_file="${AGENT_RUN_OUTPUT_FILE:-.agent-run.json}"

while (( $# > 0 )); do
  case "$1" in
    --test-outcome)
      test_outcome="${2:-}"
      shift 2
      ;;
    --checks)
      checks="${2:-}"
      shift 2
      ;;
    --handoff-file)
      handoff_file="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      ;;
  esac
done

if [[ -z "$test_outcome" ]]; then
  usage
fi

require_handoff() {
  if ! command -v jq >/dev/null 2>&1; then
    echo "jq is required to parse ${handoff_file}." >&2
    exit 1
  fi

  if [[ ! -f "$handoff_file" ]]; then
    echo "Missing required agent handoff file: ${handoff_file}" >&2
    exit 1
  fi

  if ! jq -e '
    . as $root |
    type == "object" and
    (["title", "task", "why", "summary", "observations", "next", "expectedGardenEffect"] |
      all(.[]; ($root[.] | type == "string" and length > 0))) and
    (($root.requests // []) | type == "array") and
    (all(($root.requests // [])[]; type == "object")) and
    (($root.codeMap // []) | type == "array") and
    (all(($root.codeMap // [])[];
      type == "object" and
      ((.path // "") | type == "string" and length > 0) and
      ((.description // "") | type == "string" and length > 0)
    )) and
    (($root.state // {}) | type == "object") and
    (($root.state.immediateDirections // []) | type == "array") and
    (($root.state.constraints // []) | type == "array")
  ' "$handoff_file" >/dev/null; then
    echo "Agent handoff JSON is malformed: ${handoff_file}" >&2
    exit 1
  fi
}

json_value() {
  local query="$1"
  jq -r "${query} // empty" "$handoff_file"
}

json_array_lines() {
  local query="$1"
  jq -r "${query} // [] | .[] | select(type == \"string\" and length > 0)" "$handoff_file"
}

restore_generated_memory() {
  git restore README.md agent/state.md agent/requests.md agent/code-map.md agent/templates agent/journal agent/summaries 2>/dev/null || true
  git clean -fd agent/journal agent/summaries 2>/dev/null || true
}

changed_files() {
  git status --porcelain |
    sed '/^$/d' |
    sed 's/^...//; s/.* -> //' |
    sort -u
}

changed_files_csv() {
  local files
  files="$(changed_files | paste -sd ', ' -)"
  if [[ -z "$files" ]]; then
    echo "no source files"
  else
    echo "$files"
  fi
}

changed_category() {
  if changed_files | grep -q '^src/main/java/'; then
    echo "Simulation behavior update"
  elif changed_files | grep -q '^src/test/java/'; then
    echo "Behavior test coverage update"
  elif changed_files | grep -q '^data/garden-state.txt$'; then
    echo "Garden state advancement"
  elif changed_files | grep -q '^README.md\|^agent/\|^scripts/'; then
    echo "Project maintenance update"
  else
    echo "Autonomous garden update"
  fi
}

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

types_csv() {
  organism_counts |
    awk -F= '$1 != "total" { print tolower($1) }' |
    paste -sd ', ' - |
    sed 's/_/ /g'
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
  local counts="$1"
  local total="$2"
  local nutrients="$3"
  local missing="$4"

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

write_agent_state() {
  local cycle="$1"
  local nutrients="$2"
  local buffer="$3"
  local total="$4"
  local active_types="$5"
  local missing="$6"
  local health="$7"
  local status="$8"
  local change_title="$9"
  local run_summary="${10}"
  local directions=()
  local constraints=()

  mapfile -t directions < <(json_array_lines '.state.immediateDirections')
  mapfile -t constraints < <(json_array_lines '.state.constraints')

  if (( ${#directions[@]} == 0 )); then
    directions=(
      "Prefer outcome-changing work with visible consequences for future ticks of the current or recoverable garden."
      "Consolidate or connect existing mechanics before adding another named adaptation, diagnostic, renderer line, event log, or test-only change."
      "Focus on recovery pathways for missing ecological roles, nutrient-buffer usefulness, population balance, and clearer state transitions."
    )
  fi

  if (( ${#constraints[@]} == 0 )); then
    constraints=(
      "Do not attempt to fix the simulation in one run."
      "Do not add another named adaptation merely because recent runs did so."
      "Do not add another observability-only or tests-only change merely because it is easy to validate."
      "Do not treat the full nutrient buffer as proof of health while nutrients are zero or ecological roles are absent."
    )
  fi

  {
    cat <<EOF
# Agent State

Compact current memory for future autonomous runs.

## Current Garden State

- Cycle: ${cycle}
- Health: ${status} (${health})
- Nutrients: ${nutrients}.
- NutrientBuffer: ${buffer}.
- Active organisms: ${total} total across ${active_types:-none}.
- Missing roles: ${missing}.
- Latest agent handoff: ${change_title}.
- Latest result: ${run_summary}.

## Immediate Directions

EOF
    for direction in "${directions[@]}"; do
      echo "- ${direction}"
    done
    cat <<EOF

## Constraints & Known Bad Ideas

EOF
    for constraint in "${constraints[@]}"; do
      echo "- ${constraint}"
    done
  } > agent/state.md
}

append_requests() {
  local request_count
  request_count="$(jq '.requests | length' "$handoff_file")"

  if (( request_count == 0 )); then
    return
  fi

  sed -i '/^\*No open requests yet\.\*$/d' agent/requests.md

  {
    echo
    echo "## Automated open requests from ${timestamp}"
    echo
    jq -c '.requests[]' "$handoff_file" | while IFS= read -r request; do
      local title
      local what
      local why
      local affected
      local benefit
      local fallback

      title="$(jq -r '.title // "Untitled request"' <<<"$request")"
      what="$(jq -r '.what // "-"' <<<"$request")"
      why="$(jq -r '.why // "-"' <<<"$request")"
      affected="$(jq -r '.affected // "-"' <<<"$request")"
      benefit="$(jq -r '.benefit // "-"' <<<"$request")"
      fallback="$(jq -r '.fallback // "-"' <<<"$request")"

      echo "### ${timestamp:0:10} - ${title}"
      echo
      echo "Status: Open"
      echo
      echo "What is requested: ${what}"
      echo
      echo "Why it is useful: ${why}"
      echo
      echo "Affected files or configuration: ${affected}"
      echo
      echo "Expected benefit: ${benefit}"
      echo
      echo "Fallback plan: ${fallback}"
      echo
    done
  } >> agent/requests.md
}

summary_source_headings() {
  local dir="$1"
  local pattern="$2"

  find "$dir" -maxdepth 1 -type f -name "$pattern" | sort | while IFS= read -r path; do
    printf '%s: ' "$(basename "$path" .md)"
    grep '^### ' "$path" | sed 's/^### //' | paste -sd '; ' -
  done
}

append_rollups_if_due() {
  local today
  local weekday
  local day_of_month
  local month_day
  local previous_week
  local previous_month
  local previous_year
  local body

  today="$(date -u +%Y-%m-%d)"
  weekday="$(date -u +%u)"
  day_of_month="$(date -u +%d)"
  month_day="$(date -u +%m-%d)"

  if [[ "$weekday" == "1" ]]; then
    previous_week="$(date -u -d "${today} -7 days" +%G-W%V)"
    if [[ ! -f "agent/summaries/weekly/${previous_week}.md" ]] || ! grep -q '^### ' "agent/summaries/weekly/${previous_week}.md"; then
      body="Automated weekly rollup from active daily summaries for ${previous_week}. Source entries: $(summary_source_headings agent/summaries/daily '*.md' | tail -7 | tr '\n' ' ')"
      scripts/agent-append-summary.sh --cadence weekly --period "$previous_week" --timestamp "$timestamp" --title "Automated weekly rollup" --body "$body" >/dev/null
    fi
  fi

  if [[ "$day_of_month" == "01" ]]; then
    previous_month="$(date -u -d "${today} -1 month" +%Y-%m)"
    if [[ ! -f "agent/summaries/monthly/${previous_month}.md" ]] || ! grep -q '^### ' "agent/summaries/monthly/${previous_month}.md"; then
      body="Automated monthly rollup from active weekly summaries for ${previous_month}. Source entries: $(summary_source_headings agent/summaries/weekly '*.md' | tr '\n' ' ')"
      scripts/agent-append-summary.sh --cadence monthly --period "$previous_month" --timestamp "$timestamp" --title "Automated monthly rollup" --body "$body" >/dev/null
    fi
  fi

  if [[ "$month_day" == "01-01" ]]; then
    previous_year="$(date -u -d "${today} -1 year" +%Y)"
    if [[ ! -f "agent/summaries/yearly/${previous_year}.md" ]] || ! grep -q '^### ' "agent/summaries/yearly/${previous_year}.md"; then
      body="Automated yearly rollup from active monthly summaries for ${previous_year}. Source entries: $(summary_source_headings agent/summaries/monthly "${previous_year}-*.md" | tr '\n' ' ')"
      scripts/agent-append-summary.sh --cadence yearly --period "$previous_year" --timestamp "$timestamp" --title "Automated yearly rollup" --body "$body" >/dev/null
    fi
  fi
}

require_handoff
restore_generated_memory
scripts/agent-update-code-map.sh --handoff-file "$handoff_file"

change_title="$(json_value '.title')"
task_text="$(json_value '.task')"
why_text="$(json_value '.why')"
summary_text="$(json_value '.summary')"
observations_text="$(json_value '.observations')"
next_text="$(json_value '.next')"
expected_effect="$(json_value '.expectedGardenEffect')"
expected_effect="${expected_effect%.}"
changed_list="$(changed_files_csv)"
cycle="$(state_value cycle)"
nutrients="$(state_value nutrients)"
buffer="$(state_value nutrientBuffer)"
counts="$(organism_counts)"
total="$(awk -F= '$1 == "total" { print $2 }' <<<"$counts")"
active_types="$(types_csv)"
missing="$(missing_roles_csv "$counts")"
health_record="$(health_status "$counts" "${total:-0}" "${nutrients:-0}" "$missing")"
IFS='|' read -r health_symbol health_label health_reason <<<"$health_record"

write_agent_state "$cycle" "$nutrients" "$buffer" "${total:-0}" "$active_types" "$missing" "$health_symbol" "$health_label" "$change_title" "$summary_text"
append_requests

readme_narrative="At cycle ${cycle}, the garden contains ${total:-0} organisms across ${active_types:-no active types}. Available nutrients are ${nutrients}, the nutrient buffer is ${buffer}, and missing roles are ${missing}; the next useful changes should improve recoverable ecosystem behavior rather than add bookkeeping."
scripts/agent-update-readme-state.sh --symbol "$health_symbol" --status "$health_label" --reason "$health_reason" --narrative "$readme_narrative"

summary_body="${summary_text} Expected future effect: ${expected_effect}. Changed files before memory generation: ${changed_list}. After the workflow tick, the garden reached cycle ${cycle} with nutrients ${nutrients}, nutrientBuffer ${buffer}, active types ${active_types:-none}, and missing roles ${missing}. Test validation outcome: ${test_outcome}."
scripts/agent-append-summary.sh --cadence daily --timestamp "$timestamp" --title "$change_title" --body "$summary_body" >/dev/null
append_rollups_if_due

if [[ "$test_outcome" == "success" ]]; then
  test_result="Passed."
else
  test_result="Final test validation outcome: ${test_outcome}."
fi

scripts/agent-create-journal-entry.sh \
  --timestamp "$timestamp" \
  --title "$change_title" \
  --task "$task_text" \
  --reason "$why_text" \
  --checks "$checks" \
  --test-result "$test_result" \
  --observations "${observations_text} Expected future effect: ${expected_effect}. Automated post-processing refreshed README/state memory from data/garden-state.txt at cycle ${cycle}." \
  --next "$next_text" >/dev/null

rm -f "$handoff_file"

echo "Automated agent post-processing completed."
