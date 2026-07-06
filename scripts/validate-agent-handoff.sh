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

  if ! awk -v label="$pm_direction" '$0 ~ "^### " label "[.] " { found = 1 } END { exit found ? 0 : 1 }' "$pm_plan_file"; then
    echo "Agent handoff selected pmDirection=${pm_direction}, but that direction was not found in ${pm_plan_file}." >&2
    exit 1
  fi
fi

echo "Agent handoff JSON is present and valid: ${handoff_file}"
