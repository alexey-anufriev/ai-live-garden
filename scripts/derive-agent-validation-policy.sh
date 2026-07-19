#!/usr/bin/env bash
set -euo pipefail

handoff_file="${1:-.agent-run.json}"
scripts/validate-agent-handoff.sh "$handoff_file" >/dev/null

run_mode="$(jq -r '.runMode' "$handoff_file")"
production_changed="false"
if [[ -n "$(git status --porcelain -uall -- src/main pom.xml data/garden-state.txt)" ]]; then
  production_changed="true"
fi

case "$run_mode" in
  evolution)
    shadow_policy="target"
    advance_garden="true"
    ;;
  recovery)
    shadow_policy="safety"
    advance_garden="true"
    ;;
  maintenance)
    shadow_policy="safety"
    advance_garden="false"
    ;;
  repair)
    if [[ "$production_changed" == "true" || "${AGENT_BASELINE_SHADOW_OUTCOME:-success}" != "success" ]]; then
      shadow_policy="safety"
    else
      shadow_policy="skip"
    fi
    advance_garden="false"
    ;;
  diagnostic)
    shadow_policy="skip"
    advance_garden="false"
    ;;
esac

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "run_mode=${run_mode}"
    echo "shadow_policy=${shadow_policy}"
    echo "advance_garden=${advance_garden}"
    echo "production_changed=${production_changed}"
  } >> "$GITHUB_OUTPUT"
fi

echo "Agent validation policy: mode=${run_mode}, shadow=${shadow_policy}, advanceGarden=${advance_garden}, productionChanged=${production_changed}."
