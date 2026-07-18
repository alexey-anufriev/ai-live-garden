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
timeout_seconds="${SHADOW_SIMULATION_TIMEOUT_SECONDS:-120}"
max_parallel_seeds="${SHADOW_SIMULATION_MAX_PARALLEL_SEEDS:-4}"
simulation_runner="${SHADOW_SIMULATION_RUNNER:-java}"

for value in "$steps" "$max_organisms" "$timeout_seconds" "$max_parallel_seeds"; do
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
rm -f "$output_file"
work_dir="$(mktemp -d)"
pids=()
result_files=()
seed_values=()

cleanup() {
  local pid
  for pid in "${pids[@]}"; do
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
    fi
  done
  wait "${pids[@]}" 2>/dev/null || true
  rm -rf "$work_dir"
}
trap cleanup EXIT

IFS=',' read -r -a seed_values <<< "$seeds"
if (( ${#seed_values[@]} == 0 || ${#seed_values[@]} > max_parallel_seeds )); then
  echo "Shadow simulation requires between 1 and ${max_parallel_seeds} seeds." >&2
  exit 2
fi

for index in "${!seed_values[@]}"; do
  seed="${seed_values[$index]}"
  if ! [[ "$seed" =~ ^-?[0-9]+$ ]]; then
    echo "Invalid shadow simulation seed: ${seed}" >&2
    exit 2
  fi
  result_file="${work_dir}/${index}.json"
  result_files+=("$result_file")
  (
    timeout --signal=TERM --kill-after=10s "${timeout_seconds}s" \
      "$simulation_runner" -cp target/classes garden.ai.Main simulate \
        --state "$state_file" --steps "$steps" --seed "$seed" --max-organisms "$max_organisms" > "$result_file"
  ) &
  pids+=("$!")
done

failed="false"
for index in "${!pids[@]}"; do
  exit_code=0
  wait "${pids[$index]}" || exit_code=$?
  if (( exit_code != 0 )); then
    if (( exit_code == 124 || exit_code == 137 )); then
      failure_status="timed-out"
    else
      failure_status="failed"
    fi
    jq -n \
      --argjson seed "${seed_values[$index]}" \
      --argjson requestedSteps "$steps" \
      --arg status "$failure_status" \
      --argjson exitCode "$exit_code" \
      '{seed:$seed,requestedSteps:$requestedSteps,completedSteps:0,status:$status,exitCode:$exitCode}' \
      > "${result_files[$index]}"
    echo "Shadow simulation ${failure_status} for seed ${seed_values[$index]} (exit ${exit_code})." >&2
    failed="true"
  elif ! jq -e 'type == "object" and (.seed | type == "number") and (.status | type == "string")' "${result_files[$index]}" >/dev/null 2>&1; then
    jq -n \
      --argjson seed "${seed_values[$index]}" \
      --argjson requestedSteps "$steps" \
      '{seed:$seed,requestedSteps:$requestedSteps,completedSteps:0,status:"invalid-output",exitCode:0}' \
      > "${result_files[$index]}"
    echo "Shadow simulation returned invalid output for seed ${seed_values[$index]}." >&2
    failed="true"
  fi
done

jq -s '.' "${result_files[@]}" > "$output_file"
if [[ "$failed" == "true" ]]; then
  echo "Captured failed shadow simulation diagnostics in ${output_file}." >&2
  exit 1
fi
echo "Captured shadow simulation metrics in ${output_file}."
