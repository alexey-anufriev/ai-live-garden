#!/usr/bin/env bash
set -euo pipefail

handoff_file="${1:-.agent-run.json}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to parse ${handoff_file}." >&2
  exit 1
fi

if [[ ! -f "$handoff_file" ]]; then
  echo "Missing required agent handoff file: ${handoff_file}" >&2
  echo "The Gemini step completed without writing the machine-readable handoff required by the autonomous-run contract." >&2
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
  echo "Expected the exact .agent-run.json object shape described in the autonomous prompt." >&2
  exit 1
fi

repair_allowed=false
if [[ "${AGENT_BASELINE_TEST_OUTCOME:-}" == "failure" || "${AGENT_BASELINE_POLICY_OUTCOME:-}" == "failure" || "${AGENT_ALLOW_BEHAVIOR_NEUTRAL_HANDOFF:-}" == "true" ]]; then
  repair_allowed=true
fi

purpose_text="$(jq -r '[.title, .task, .why, .summary] | join("\n") | ascii_downcase' "$handoff_file")"
effect_text="$(jq -r '.expectedGardenEffect | ascii_downcase' "$handoff_file")"
all_handoff_text="$(jq -r '[.title, .task, .why, .summary, .observations, .next, .expectedGardenEffect] | join("\n") | ascii_downcase' "$handoff_file")"

if [[ "${AGENT_ALLOW_BEHAVIOR_NEUTRAL_HANDOFF:-}" != "true" && ( "${AGENT_BASELINE_TEST_OUTCOME:-}" == "failure" || "${AGENT_BASELINE_POLICY_OUTCOME:-}" == "failure" ) ]]; then
  if ! grep -Eq 'repair|fix|restore|baseline|policy|maven|test|compil|validation|violation' <<<"$purpose_text"; then
    echo "Agent handoff is running under a failing baseline but does not describe repair work." >&2
    echo "Repair runs may be behavior-neutral only when the chosen task is the required baseline repair." >&2
    exit 1
  fi
fi

if [[ "$repair_allowed" != "true" ]]; then
  if grep -Eq 'no immediate (behavior|behaviour|effect|change)|no (functional|behavioral|behavioural) changes?|without changing (behavior|behaviour|functionality)|behavior-neutral|behaviour-neutral|easier future maintenance|easier maintenance|cleaner code|cleaner structure|centraliz(e|ed|es|ing|ation)|refactor(ed|ing)?|consolidat(e|ed|es|ing|ion)|readability|maintainability|extensibility|simpler future changes|future maintenance' <<<"$effect_text"; then
    echo "Agent handoff expectedGardenEffect is behavior-neutral or maintainability-only." >&2
    echo "Normal autonomous runs must describe a future ecological/runtime consequence, not refactoring value." >&2
    exit 1
  fi

  if grep -Eq 'no immediate (behavior|behaviour|effect|change)|no (functional|behavioral|behavioural) changes?|behavior-neutral|behaviour-neutral' <<<"$purpose_text"; then
    echo "Agent handoff describes behavior-neutral work outside a repair run." >&2
    echo "Normal autonomous runs must choose behavior-changing garden value." >&2
    exit 1
  fi

  if ! grep -Eq 'future tick|future ticks|tick|cycle|garden|organism|population|nutrient|buffer|starv|reproduc|predat|decay|decompos|fung|moss|root|herbivore|predator|beetle|fox|hare|plant|spore|role|surviv|energy|environment|drought|stress|recover|coloniz|succession|food chain' <<<"$effect_text"; then
    echo "Agent handoff expectedGardenEffect does not name an observable ecological/runtime consequence." >&2
    echo "Describe how future ticks, organisms, roles, nutrients, survival, reproduction, predation, decay, or environment should change." >&2
    exit 1
  fi

  pm_plan_file="${AGENT_PM_PLAN_FILE:-}"
  if [[ -z "$pm_plan_file" ]]; then
    pm_plan_file="$(find agent/plans -maxdepth 1 -type f -name '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].md' -print 2>/dev/null | sort -V | tail -n 1)"
  fi
  if [[ -f "$pm_plan_file" ]]; then
    pm_direction="$(jq -r '.pmDirection // empty' "$handoff_file")"
    if ! grep -Eq '^[A-D]$' <<<"$pm_direction"; then
      echo "Agent handoff must set pmDirection to A, B, C, or D when ${pm_plan_file} exists." >&2
      echo "The latest PM plan is the highest product priority for normal runs." >&2
      exit 1
    fi

    pm_title="$(awk -v label="$pm_direction" '
      $0 ~ "^### " label "[.] " {
        sub("^### " label "[.] ", "")
        print
        exit
      }
    ' "$pm_plan_file")"

    if [[ -z "$pm_title" ]]; then
      echo "Agent handoff selected pmDirection=${pm_direction}, but that direction was not found in ${pm_plan_file}." >&2
      exit 1
    fi

    pm_title_keywords="$(tr '[:upper:]' '[:lower:]' <<<"$pm_title" |
      tr -cs '[:alnum:]' '\n' |
      awk 'length($0) >= 4 && $0 !~ /^(make|this|that|with|from|into|under|over|more|less|future|garden|direction)$/ { print }')"

    if [[ -n "$pm_title_keywords" ]]; then
      matched_keyword=false
      while IFS= read -r keyword; do
        [[ -n "$keyword" ]] || continue
        if grep -Fq "$keyword" <<<"$all_handoff_text"; then
          matched_keyword=true
          break
        fi
      done <<<"$pm_title_keywords"

      if [[ "$matched_keyword" != "true" ]]; then
        echo "Agent handoff selected PM direction ${pm_direction} (${pm_title}) but does not appear to pursue that direction." >&2
        echo "Mention the selected PM direction's topic in the task, why, summary, or expectedGardenEffect." >&2
        exit 1
      fi
    fi
  fi
fi

echo "Agent handoff JSON is present and valid: ${handoff_file}"
