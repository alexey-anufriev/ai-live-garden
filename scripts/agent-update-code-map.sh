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

load_existing_descriptions() {
  local line
  local path
  local description

  [[ -f "$output_file" ]] || return

  while IFS= read -r line; do
    if [[ "$line" =~ ^-\ \`([^\`]+)\`:\ (.*)$ ]]; then
      path="${BASH_REMATCH[1]}"
      description="${BASH_REMATCH[2]}"
      description="${description%.}"
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
    echo "${descriptions[$path]}"
  else
    java_declarations "$path"
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
