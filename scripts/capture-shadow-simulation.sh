#!/usr/bin/env bash
set -euo pipefail

output_file="${1:-}"
if [[ -z "$output_file" ]]; then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

state_file="${SHADOW_STATE_FILE:-data/garden-state.txt}"
steps="${SHADOW_SIMULATION_STEPS:-5}"
seeds="${SHADOW_SIMULATION_SEEDS:-17,43}"
max_organisms="${SHADOW_MAX_ORGANISMS:-25000}"
timeout_seconds="${SHADOW_SIMULATION_TIMEOUT_SECONDS:-90}"

for value in "$steps" "$max_organisms" "$timeout_seconds"; do
  if ! [[ "$value" =~ ^[1-9][0-9]*$ ]]; then
    echo "Shadow simulation numeric settings must be positive integers." >&2
    exit 2
  fi
done

if [[ ! -f target/classes/garden/ai/Main.class ]]; then
  echo "Compiled simulation classes are missing; run Maven tests before shadow evaluation." >&2
  exit 1
fi

mkdir -p "$(dirname "$output_file")"
json_lines="$(mktemp)"
trap 'rm -f "$json_lines"' EXIT

for seed in ${seeds//,/ }; do
  if ! [[ "$seed" =~ ^-?[0-9]+$ ]]; then
    echo "Invalid shadow simulation seed: ${seed}" >&2
    exit 2
  fi
  timeout --signal=TERM --kill-after=10s "${timeout_seconds}s" \
    java -cp target/classes garden.ai.Main simulate \
      --state "$state_file" --steps "$steps" --seed "$seed" --max-organisms "$max_organisms" >> "$json_lines"
done

jq -s '.' "$json_lines" > "$output_file"
echo "Captured shadow simulation metrics in ${output_file}."
