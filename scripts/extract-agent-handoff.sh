#!/usr/bin/env bash
set -euo pipefail

output_file=".agent-run.json"
inputs=()

usage() {
  cat >&2 <<'USAGE'
Usage:
  scripts/extract-agent-handoff.sh [--output PATH] [INPUT ...]

Creates the agent handoff file from Gemini output when the model did not
write it directly. INPUT may be a file or directory. Directories are scanned
recursively for text-like files.
USAGE
  exit 2
}

while (( $# > 0 )); do
  case "$1" in
    --output)
      output_file="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      ;;
    --)
      shift
      break
      ;;
    -*)
      echo "Unknown argument: $1" >&2
      usage
      ;;
    *)
      inputs+=("$1")
      shift
      ;;
  esac
done

if [[ -z "$output_file" ]]; then
  usage
fi

if [[ -f "$output_file" ]]; then
  scripts/validate-agent-handoff.sh "$output_file"
  exit 0
fi

if (( $# > 0 )); then
  inputs+=("$@")
fi

if (( ${#inputs[@]} == 0 )); then
  inputs=(gemini-artifacts)
fi

candidate_files() {
  local input
  for input in "${inputs[@]}"; do
    if [[ -f "$input" ]]; then
      echo "$input"
    elif [[ -d "$input" ]]; then
      find "$input" -type f \
        ! -name '*.zip' \
        ! -name '*.png' \
        ! -name '*.jpg' \
        ! -name '*.jpeg' \
        ! -name '*.gif' \
        ! -name '*.pdf' \
        -print
    fi
  done
}

valid_handoff() {
  local path="$1"
  scripts/validate-agent-handoff.sh "$path" >/dev/null 2>&1
}

try_candidate() {
  local source_file="$1"
  local candidate_file="$2"

  [[ -s "$candidate_file" ]] || return 1
  jq '.' "$candidate_file" > "${candidate_file}.normalized" 2>/dev/null || return 1
  mv "${candidate_file}.normalized" "$candidate_file"

  if valid_handoff "$candidate_file"; then
    mv "$candidate_file" "$output_file"
    echo "Extracted agent handoff from ${source_file} into ${output_file}."
    return 0
  fi

  return 1
}

extract_marked_block() {
  local source_file="$1"
  local candidate_file="$2"

  awk '
    /^[[:space:]]*AGENT_RUN_JSON_START[[:space:]]*$/ { capture = 1; next }
    /^[[:space:]]*AGENT_RUN_JSON_END[[:space:]]*$/ { if (capture) exit }
    capture { print }
  ' "$source_file" > "$candidate_file"
}

extract_json_fence() {
  local source_file="$1"
  local candidate_file="$2"

  awk '
    /^[[:space:]]*```json[[:space:]]*$/ { capture = 1; next }
    /^[[:space:]]*```[[:space:]]*$/ { if (capture) exit }
    capture { print }
  ' "$source_file" > "$candidate_file"
}

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

while IFS= read -r source_file; do
  [[ -n "$source_file" && -f "$source_file" ]] || continue

  marked_candidate="${tmp_dir}/marked.json"
  extract_marked_block "$source_file" "$marked_candidate"
  if try_candidate "$source_file" "$marked_candidate"; then
    exit 0
  fi

  fenced_candidate="${tmp_dir}/fenced.json"
  extract_json_fence "$source_file" "$fenced_candidate"
  if try_candidate "$source_file" "$fenced_candidate"; then
    exit 0
  fi

  whole_file_candidate="${tmp_dir}/whole-file.json"
  cp "$source_file" "$whole_file_candidate"
  if try_candidate "$source_file" "$whole_file_candidate"; then
    exit 0
  fi
done < <(candidate_files | sort -u)

echo "Could not create ${output_file}: no valid agent handoff JSON was found in Gemini output." >&2
echo "Expected either ${output_file} or a JSON block between AGENT_RUN_JSON_START and AGENT_RUN_JSON_END." >&2
exit 1
