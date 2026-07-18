#!/usr/bin/env bash
set -euo pipefail

required_ticks="${AGENT_MIN_TICKS_BETWEEN_RUNS:-3}"
if ! [[ "$required_ticks" =~ ^[0-9]+$ ]]; then
  echo "AGENT_MIN_TICKS_BETWEEN_RUNS must be a non-negative integer." >&2
  exit 2
fi

last_agent="$(git log --grep='^feat: autonomous garden evolution' --format='%H' -n 1 2>/dev/null || true)"
if [[ -z "$last_agent" ]]; then
  echo "No earlier autonomous evolution commit; observation window is open."
  exit 0
fi

observed_ticks="$(git log "${last_agent}..HEAD" --format='%s' | awk '/^feat: garden tick/ { count++ } END { print count + 0 }')"
if (( observed_ticks < required_ticks )); then
  echo "Observation window is still active: ${observed_ticks}/${required_ticks} AI-less ticks since the last autonomous change." >&2
  exit 1
fi

echo "Observation window satisfied: ${observed_ticks}/${required_ticks} AI-less ticks since the last autonomous change."
