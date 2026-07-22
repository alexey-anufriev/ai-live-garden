#!/usr/bin/env bash
set -euo pipefail

handoff_file="${1:-.agent-run.json}"

if [[ ! -f "$handoff_file" ]] || ! jq -e 'type == "object"' "$handoff_file" >/dev/null 2>&1; then
  exit 0
fi

changes=()

rewrite() {
  local filter="$1"
  shift
  local temporary
  temporary="$(mktemp "${RUNNER_TEMP:-/tmp}/agent-handoff-normalized.XXXXXX")"
  if jq "$@" "$filter" "$handoff_file" > "$temporary"; then
    mv "$temporary" "$handoff_file"
  else
    rm -f "$temporary"
    return 1
  fi
}

record_change() {
  changes+=("$1")
}

run_mode="$(jq -r '.runMode // empty' "$handoff_file")"
baseline_healthy="true"
for outcome in \
  "${AGENT_BASELINE_TEST_OUTCOME:-success}" \
  "${AGENT_BASELINE_POLICY_OUTCOME:-success}" \
  "${AGENT_BASELINE_SHADOW_OUTCOME:-success}"; do
  if [[ "$outcome" != "success" ]]; then
    baseline_healthy="false"
  fi
done

# Agents often use "recovery" colloquially for an ecological recovery task.
# Without a failed baseline or a persisted-state intervention, that is a normal
# measured evolution and should not lose otherwise valid source code.
if [[ "$run_mode" == "recovery" && "$baseline_healthy" == "true" \
    && -z "$(git status --porcelain -uall -- data/garden-state.txt)" ]]; then
  rewrite '.runMode = "evolution" | .acceptanceSource = "agent"'
  record_change "reclassified ineligible recovery as measured evolution"
  run_mode="evolution"
fi

pm_plan_file="${AGENT_PM_PLAN_FILE:-}"
if [[ -z "$pm_plan_file" ]]; then
  pm_plan_file="$(scripts/find-active-agent-plan.sh)"
fi

if [[ "$run_mode" == "evolution" ]]; then
  acceptance_source="$(jq -r '.acceptanceSource // empty' "$handoff_file")"
  if [[ "$acceptance_source" != "agent" && "$acceptance_source" != "pm" ]]; then
    rewrite '.acceptanceSource = "agent"'
    record_change "canonicalized evolution acceptance source to agent"
  fi

  if [[ -z "$pm_plan_file" ]]; then
    if [[ "$(jq -r '.pmDirection // empty' "$handoff_file")" != "none" ]]; then
      rewrite '.pmDirection = "none"'
      record_change "cleared PM direction because no active plan exists"
    fi
  elif ! jq -e '.pmDirection | type == "string" and test("^[A-D]$")' "$handoff_file" >/dev/null 2>&1; then
    pm_json_file="${pm_plan_file%.md}.json"
    inferred_direction=""
    if [[ -f "$pm_json_file" ]]; then
      inferred_direction="$(jq -r --slurpfile handoff "$handoff_file" '
        first(.directions[] |
          select(.shadowAcceptance.metric == $handoff[0].evaluation.metric and
                 .shadowAcceptance.goal == $handoff[0].evaluation.goal) |
          .label) // empty
      ' "$pm_json_file")"
    fi
    if [[ "$inferred_direction" =~ ^[A-D]$ ]]; then
      rewrite '.pmDirection = $direction' --arg direction "$inferred_direction"
      record_change "inferred PM direction ${inferred_direction} from the ecological target"
    fi
  fi

  if [[ "$(jq -r '.acceptanceSource // empty' "$handoff_file")" == "pm" ]]; then
    pm_json_file="${pm_plan_file%.md}.json"
    if [[ ! -f "$pm_json_file" ]] || ! jq -e --arg label "$(jq -r '.pmDirection' "$handoff_file")" \
        --slurpfile handoff "$handoff_file" '
          first(.directions[] | select(.label == $label) | .shadowAcceptance) == $handoff[0].evaluation
        ' "$pm_json_file" >/dev/null 2>&1; then
      rewrite '.acceptanceSource = "agent"'
      record_change "treated a non-default PM target as agent-selected acceptance"
    fi
  fi
else
  if [[ "$(jq -r '.pmDirection // empty' "$handoff_file")" != "none" ]]; then
    rewrite '.pmDirection = "none"'
    record_change "canonicalized non-evolution PM direction to none"
  fi
  if [[ "$run_mode" == "repair" || "$run_mode" == "recovery" || "$run_mode" == "diagnostic" ]]; then
    if ! jq -e '.acceptanceSource == "mode" and .evaluation == {metric:"tests",goal:"pass",requiredDelta:0}' \
        "$handoff_file" >/dev/null 2>&1; then
      rewrite '.acceptanceSource = "mode" | .evaluation = {metric:"tests", goal:"pass", requiredDelta:0}'
      record_change "canonicalized ${run_mode} acceptance to tests/pass/0"
    fi
  elif [[ "$run_mode" == "maintenance" && "$(jq -r '.acceptanceSource // empty' "$handoff_file")" != "mode" ]]; then
    rewrite '.acceptanceSource = "mode"'
    record_change "canonicalized maintenance acceptance source to mode"
  fi
fi

if [[ "$(jq -r '(.causalReach // null) | type' "$handoff_file")" == "object" ]]; then
  carrier_basis="$(jq -r '.causalReach.carrierBasis // empty' "$handoff_file")"
  case "$carrier_basis" in
    not-applicable)
      if ! jq -e '.causalReach.traits == [] and .causalReach.activeCarrierCount == 0' \
          "$handoff_file" >/dev/null 2>&1; then
        rewrite '.causalReach.traits = [] | .causalReach.activeCarrierCount = 0'
        record_change "canonicalized global mechanism carrier metadata"
      fi
      ;;
    existing|adoption)
      if jq -e '.causalReach.traits | type == "array" and all(.[]; type == "string" and test("^[a-z0-9-]+$"))' \
          "$handoff_file" >/dev/null 2>&1; then
        causal_state_file="${AGENT_GARDEN_STATE_FILE:-data/garden-state.txt}"
        actual_carriers=0
        while IFS= read -r causal_trait; do
          [[ -n "$causal_trait" ]] || continue
          trait_carriers="$(scripts/count-garden-trait-carriers.sh "$causal_trait" "$causal_state_file")"
          actual_carriers=$((actual_carriers + trait_carriers))
        done < <(jq -r '.causalReach.traits[]' "$handoff_file")
        if [[ "$(jq -r '.causalReach.activeCarrierCount // -1' "$handoff_file")" != "$actual_carriers" ]]; then
          rewrite '.causalReach.activeCarrierCount = $count' --argjson count "$actual_carriers"
          record_change "recalculated active trait carriers as ${actual_carriers}"
        fi
      fi
      ;;
  esac
fi

if [[ -f agent/shadow-feedback.md \
    && "$(jq -r '.causalReach.previousFeedbackDecision // empty' "$handoff_file")" == "none" ]]; then
  rewrite '.causalReach.previousFeedbackDecision = "revise"'
  record_change "defaulted supplied-feedback decision to revise"
elif [[ ! -f agent/shadow-feedback.md \
    && "$(jq -r '.causalReach.previousFeedbackDecision // empty' "$handoff_file")" =~ ^(reuse|revise|abandon)$ ]]; then
  rewrite '.causalReach.previousFeedbackDecision = "none"'
  record_change "cleared feedback decision because no feedback is present"
fi

if (( ${#changes[@]} > 0 )); then
  changes_json="$(printf '%s\n' "${changes[@]}" | jq -R . | jq -s .)"
  rewrite '.harnessNormalization = {applied: $changes}' --argjson changes "$changes_json"
  normalization_summary="$(printf '%s; ' "${changes[@]}")"
  printf 'Normalized agent handoff: %s\n' "${normalization_summary%; }" >&2
fi
