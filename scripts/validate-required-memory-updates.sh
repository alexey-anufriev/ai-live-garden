#!/usr/bin/env bash
set -euo pipefail

violations_file="${VALIDATE_REPAIR_VIOLATIONS_FILE:-${REQUIRED_MEMORY_VIOLATIONS_FILE:-}}"

if [[ -z "$violations_file" ]]; then
  echo "VALIDATE_REPAIR_VIOLATIONS_FILE or REQUIRED_MEMORY_VIOLATIONS_FILE is required." >&2
  exit 2
fi

: > "$violations_file"

failed=0

record_violation() {
  local path="$1"
  local message="$2"

  echo "Required memory update violation in ${path}: ${message}"
  printf '%s\t%s\n' "$path" "$message" >> "$violations_file"
  failed=1
}

changed_path() {
  local path="$1"

  git diff --quiet -- "$path" && [[ ! -e "$path" || -z "$(git ls-files --others --exclude-standard -- "$path")" ]]
}

if changed_path "agent/state.md"; then
  record_violation "agent/state.md" "every autonomous run must update current project memory in place"
fi

if changed_path "README.md"; then
  record_violation "README.md" "every autonomous run must update the protected Current Garden State block"
fi

if (( failed != 0 )); then
  echo "Required mutable memory updates are missing."
  exit 1
fi

echo "Required mutable memory files were updated."
