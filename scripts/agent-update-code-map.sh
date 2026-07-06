#!/usr/bin/env bash
set -euo pipefail

output_file="agent/code-map.md"
handoff_file=""

while (( $# > 0 )); do
  case "$1" in
    --handoff-file)
      handoff_file="${2:-}"
      shift 2
      ;;
    -h|--help)
      echo "Usage: $0 [--handoff-file PATH]" >&2
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

declare -A descriptions=()

normalize_description() {
  local description="$1"

  description="${description//$'\r'/}"
  description="${description//$'\t'/ }"
  description="$(sed -E 's/[[:space:]]+/ /g; s/^ //; s/ $//; s/[.]+$//' <<<"$description")"
  echo "$description"
}

default_description_for() {
  local path="$1"

  case "$path" in
    src/main/java/garden/ai/Environment.java)
      echo "Immutable environmental resources and nutrient-buffer operations used by each garden cycle"
      ;;
    src/main/java/garden/ai/Garden.java)
      echo "Core simulation state and cycle transition logic for organisms, environment, and events"
      ;;
    src/main/java/garden/ai/GardenEvent.java)
      echo "Compact event record used by simulation and rendering"
      ;;
    src/main/java/garden/ai/GardenRenderer.java)
      echo "CLI rendering for inspect and tick output"
      ;;
    src/main/java/garden/ai/GardenStateStore.java)
      echo "Persistence for \`data/garden-state.txt\`"
      ;;
    src/main/java/garden/ai/Main.java)
      echo "CLI entry point for \`inspect\` and \`tick\`"
      ;;
    src/main/java/garden/ai/Organism.java)
      echo "Immutable organism value and per-organism attributes"
      ;;
    src/main/java/garden/ai/OrganismType.java)
      echo "Organism taxonomy, roles, prey, metabolism, and succession rules"
      ;;
    src/main/java/garden/ai/Simulation.java)
      echo "Advances seed or loaded garden state"
      ;;
    *)
      java_declarations "$path"
      ;;
  esac
}

load_existing_descriptions() {
  local line
  local path
  local description

  [[ -f "$output_file" ]] || return

  while IFS= read -r line; do
    if [[ "$line" =~ ^-\ \`([^\`]+)\`:\ (.*)$ ]]; then
      path="${BASH_REMATCH[1]}"
      description="${BASH_REMATCH[2]}"
      description="$(normalize_description "$description")"
      [[ -n "$description" ]] || continue
      descriptions["$path"]="$description"
    fi
  done < "$output_file"
}

load_handoff_descriptions() {
  local row
  local path
  local description

  [[ -n "$handoff_file" && -f "$handoff_file" ]] || return 0
  command -v jq >/dev/null 2>&1 || return 0

  while IFS=$'\t' read -r path description; do
    [[ -n "$path" && -n "$description" ]] || continue
    description="$(normalize_description "$description")"
    [[ -n "$description" ]] || continue
    descriptions["$path"]="$description"
  done < <(
    jq -r '
      (.codeMap // [])[] |
      select(type == "object") |
      (.path // "") as $path |
      (.description // "") as $description |
      select(($path | type) == "string" and ($path | length) > 0) |
      select(($description | type) == "string" and ($description | length) > 0) |
      [$path, $description] | @tsv
    ' "$handoff_file"
  )
}

java_declarations() {
  local path="$1"
  local declarations

  declarations="$(
    sed -nE 's/^[[:space:]]*(public[[:space:]]+)?(final[[:space:]]+|sealed[[:space:]]+|abstract[[:space:]]+)*(class|enum|interface|record)[[:space:]]+([A-Za-z0-9_]+).*/\3 \4/p' "$path" |
      paste -sd ',' - |
      sed 's/,/, /g'
  )"

  if [[ -z "$declarations" ]]; then
    echo "Java source"
  else
    echo "$declarations"
  fi
}

test_count() {
  find src/test/java -type f -name '*.java' 2>/dev/null | wc -l | tr -d '[:space:]'
}

description_for() {
  local path="$1"

  if [[ -n "${descriptions[$path]:-}" ]]; then
    normalize_description "${descriptions[$path]}"
  else
    default_description_for "$path"
  fi
}

load_existing_descriptions
load_handoff_descriptions

{
  cat <<'EOF'
# Code Map

Generated source orientation for autonomous runs. Do not edit manually.

## Source Files

EOF

  if find src/main/java -type f -name '*.java' -print -quit 2>/dev/null | grep -q .; then
    while IFS= read -r path; do
      printf -- '- `%s`: %s.\n' "$path" "$(description_for "$path")"
    done < <(find src/main/java -type f -name '*.java' | sort)
  else
    echo "- No main Java source files found."
  fi

  cat <<EOF

## Tests

- Tests live under \`src/test/java/garden/ai/\`.
- Current Java test files: $(test_count).
- Prefer focused behavior tests for changed simulation rules.
- Add or inspect exact test files only when the chosen task needs them.
EOF
} > "$output_file"
