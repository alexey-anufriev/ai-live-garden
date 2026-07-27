#!/usr/bin/env bash
set -euo pipefail

plan_file="${1:-}"
state_file="${2:-${AGENT_GARDEN_STATE_FILE:-data/garden-state.txt}}"

if [[ -z "$plan_file" || ! -f "$plan_file" ]]; then
  echo "Usage: $0 PLAN_JSON [STATE_FILE]" >&2
  exit 2
fi

if ! jq -e 'type == "object"' "$plan_file" >/dev/null; then
  echo "Plan sidecar is not valid JSON: ${plan_file}" >&2
  exit 1
fi

if ! jq -e '.stateSnapshot | type == "object"' "$plan_file" >/dev/null; then
  jq -n --arg plan "$plan_file" '{status:"unknown", plan:$plan, reasons:["plan has no state snapshot"]}'
  exit 0
fi

current_snapshot="$(scripts/capture-garden-state-snapshot.sh "$state_file")"
jq -n --slurpfile plan "$plan_file" --argjson current "$current_snapshot" '
  def absolute: if . < 0 then -. else . end;
  def environment_drift($before; $after):
    ["light", "moisture", "warmth", "nutrients", "nutrientBuffer"]
    | map(select((($before.environment[.] // 0) - ($after.environment[.] // 0) | absolute) >= 20)
          | "environment." + .);
  def population_drift($before; $after):
    (($before.populations // {}) + ($after.populations // {}) | keys[]) as $type
    | (($before.populations[$type] // 0) - ($after.populations[$type] // 0) | absolute) as $difference
    | ($before.populations[$type] // 0) as $baseline
    | select($difference >= 25 and $difference >= ($baseline * 0.25))
    | "population." + $type;

  $plan[0].stateSnapshot as $before |
  (environment_drift($before; $current) + [population_drift($before; $current)]) as $reasons |
  {
    status: (if ($reasons | length) > 0 then "stale" else "fresh" end),
    reasons: $reasons,
    planSnapshot: $before,
    currentSnapshot: $current
  }
'
