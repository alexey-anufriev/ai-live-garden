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
  ($root.evidence | type == "object") and
  (all(["bottleneck", "currentState", "verification"][];
    ($root.evidence[.] | type == "string" and length > 0)
  )) and
  ($root.evaluation | type == "object") and
  (($root.evaluation.metric // "") | type == "string" and
    test("^(population[.](MOSS|ROOT_NETWORK|SPORE|FERN|FUNGUS|BEETLE|HARE|FOX)|totalOrganisms|nutrients|nutrientBuffer|tests)$")) and
  (($root.evaluation.goal // "") | type == "string" and test("^(increase|decrease|preserve|pass)$")) and
  (($root.evaluation.requiredDelta // -1) | type == "number" and . >= 0) and
  (($root.state // {}) | type == "object") and
  (($root.state.immediateDirections // []) | type == "array") and
  (($root.state.constraints // []) | type == "array")
' "$handoff_file" >/dev/null; then
  echo "Agent handoff JSON is malformed: ${handoff_file}" >&2
  echo "Expected the exact .agent-run.json object shape described in the autonomous prompt." >&2
  exit 1
fi

pm_plan_file="${AGENT_PM_PLAN_FILE:-}"
if [[ -z "$pm_plan_file" ]]; then
  pm_plan_file="$(scripts/find-active-agent-plan.sh)"
fi
pm_direction="$(jq -r '.pmDirection // empty' "$handoff_file")"
repair_required="false"
if [[ "${AGENT_BASELINE_TEST_OUTCOME:-success}" != "success" || "${AGENT_BASELINE_POLICY_OUTCOME:-success}" != "success" || "${AGENT_BASELINE_SHADOW_OUTCOME:-success}" != "success" ]]; then
  repair_required="true"
fi

if [[ "$repair_required" == "true" ]]; then
  if [[ "$pm_direction" != "none" ]]; then
    echo "Agent handoff must set pmDirection to none for a required baseline repair." >&2
    exit 1
  fi
  if [[ "$(jq -r '.evaluation.metric' "$handoff_file")" != "tests" || "$(jq -r '.evaluation.goal' "$handoff_file")" != "pass" ]]; then
    echo "A baseline repair handoff must evaluate metric=tests with goal=pass." >&2
    exit 1
  fi
elif [[ -f "$pm_plan_file" ]]; then
  if ! grep -Eq '^[A-D]$' <<<"$pm_direction"; then
    echo "Agent handoff must set pmDirection to A, B, C, or D when active plan ${pm_plan_file} exists." >&2
    echo "The active PM plan is the highest product priority for normal runs." >&2
    exit 1
  fi

  if ! awk -v label="$pm_direction" '$0 ~ "^### " label "[.] " { found = 1 } END { exit found ? 0 : 1 }' "$pm_plan_file"; then
    echo "Agent handoff selected pmDirection=${pm_direction}, but that direction was not found in ${pm_plan_file}." >&2
    exit 1
  fi
elif [[ "$pm_direction" != "none" ]]; then
  echo "Agent handoff must set pmDirection to none when no active PM plan exists." >&2
  exit 1
fi

if [[ "$repair_required" != "true" ]]; then
  evaluation_metric="$(jq -r '.evaluation.metric' "$handoff_file")"
  evaluation_goal="$(jq -r '.evaluation.goal' "$handoff_file")"
  evaluation_delta="$(jq -r '.evaluation.requiredDelta' "$handoff_file")"
  if [[ "$evaluation_metric" == "tests" || "$evaluation_goal" == "pass" ]]; then
    echo "A normal evolution handoff must declare an ecological shadow-evaluation target." >&2
    exit 1
  fi
  if [[ "$evaluation_goal" != "preserve" ]] && awk -v delta="$evaluation_delta" 'BEGIN { exit !(delta <= 0) }'; then
    echo "Increase/decrease shadow targets must require a positive delta." >&2
    exit 1
  fi
fi

echo "Agent handoff JSON is present and valid: ${handoff_file}"
