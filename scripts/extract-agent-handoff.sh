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
  scripts/validate-agent-handoff.sh "$path" >/dev/null
}

try_candidate() {
  local source_file="$1"
  local candidate_file="$2"

  [[ -s "$candidate_file" ]] || return 1
  jq '.' "$candidate_file" > "${candidate_file}.normalized" 2>/dev/null || return 1
  mv "${candidate_file}.normalized" "$candidate_file"

  candidate_error="${tmp_dir}/candidate-error.txt"
  if valid_handoff "$candidate_file" 2> "$candidate_error"; then
    mv "$candidate_file" "$output_file"
    echo "Extracted agent handoff from ${source_file} into ${output_file}."
    return 0
  fi

  first_error="$(sed -n '/[^[:space:]]/ { p; q; }' "$candidate_error")"
  if [[ -n "$first_error" ]]; then
    printf 'Invalid agent handoff candidate: %s\n' "$source_file" >> "$validation_log"
    printf 'Handoff validation error: %s\n' "$first_error" >> "$validation_log"
  fi

  return 1
}

try_source_text() {
  local source_label="$1"
  local source_text_file="$2"
  local marked_candidate
  local fenced_candidate
  local whole_file_candidate

  marked_candidate="${tmp_dir}/marked.json"
  extract_marked_block "$source_text_file" "$marked_candidate"
  if try_candidate "$source_label" "$marked_candidate"; then
    return 0
  fi

  fenced_candidate="${tmp_dir}/fenced.json"
  extract_json_fence "$source_text_file" "$fenced_candidate"
  if try_candidate "$source_label" "$fenced_candidate"; then
    return 0
  fi

  whole_file_candidate="${tmp_dir}/whole-file.json"
  cp "$source_text_file" "$whole_file_candidate"
  if try_candidate "$source_label" "$whole_file_candidate"; then
    return 0
  fi

  return 1
}

extract_json_string_field() {
  local source_file="$1"
  local field="$2"
  local output_path="$3"

  jq -er --arg field "$field" '
    if type == "object" and (.[$field] | type == "string") then
      .[$field]
    else
      empty
    end
  ' "$source_file" > "$output_path" 2>/dev/null
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
validation_log="${tmp_dir}/validation-errors.txt"

while IFS= read -r source_file; do
  [[ -n "$source_file" && -f "$source_file" ]] || continue

  if try_source_text "$source_file" "$source_file"; then
    exit 0
  fi

  for json_field in response text content output; do
    decoded_text="${tmp_dir}/decoded-${json_field}.txt"
    if extract_json_string_field "$source_file" "$json_field" "$decoded_text" && [[ -s "$decoded_text" ]]; then
      if try_source_text "${source_file}.${json_field}" "$decoded_text"; then
        exit 0
      fi
    fi
  done
done < <(candidate_files | sort -u)

if [[ -s "$validation_log" ]]; then
  awk '!seen[$0]++' "$validation_log" >&2
fi
echo "Could not create ${output_file}: no valid agent handoff JSON was found in Gemini output." >&2
echo "Expected either ${output_file}, a JSON block between AGENT_RUN_JSON_START and AGENT_RUN_JSON_END, or a valid handoff inside a known JSON output field such as .response." >&2
exit 1
