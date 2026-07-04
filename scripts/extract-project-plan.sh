#!/usr/bin/env bash
set -euo pipefail

output_file=".project-plan.json"
inputs=()

usage() {
  cat >&2 <<'USAGE'
Usage:
  scripts/extract-project-plan.sh [--output PATH] [INPUT ...]

Creates the project-manager plan JSON from Gemini output when the model did
not write it directly. INPUT may be a file or directory. Directories are
scanned recursively for text-like files.
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
  scripts/validate-project-plan.sh "$output_file"
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

valid_plan() {
  local path="$1"
  scripts/validate-project-plan.sh "$path" >/dev/null 2>&1
}

try_candidate() {
  local source_file="$1"
  local candidate_file="$2"

  [[ -s "$candidate_file" ]] || return 1
  jq '.' "$candidate_file" > "${candidate_file}.normalized" 2>/dev/null || return 1
  mv "${candidate_file}.normalized" "$candidate_file"

  if valid_plan "$candidate_file"; then
    mv "$candidate_file" "$output_file"
    echo "Extracted project plan from ${source_file} into ${output_file}."
    return 0
  fi

  return 1
}

extract_marked_block() {
  local source_file="$1"
  local candidate_file="$2"

  awk '
    /^[[:space:]]*PROJECT_PLAN_JSON_START[[:space:]]*$/ { capture = 1; next }
    /^[[:space:]]*PROJECT_PLAN_JSON_END[[:space:]]*$/ { if (capture) exit }
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

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

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

echo "Could not create ${output_file}: no valid project plan JSON was found in Gemini output." >&2
echo "Expected either ${output_file}, a JSON block between PROJECT_PLAN_JSON_START and PROJECT_PLAN_JSON_END, or a valid plan inside a known JSON output field such as .response." >&2
exit 1
