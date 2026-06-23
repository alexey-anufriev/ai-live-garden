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

readme_state_block() {
  awk '
    /^<!-- AI-LIVE-GARDEN:STATE-START -->$/ {
      in_block = 1
      next
    }
    /^<!-- AI-LIVE-GARDEN:STATE-END -->$/ {
      exit
    }
    in_block {
      print
    }
  ' README.md
}

validate_readme_state_block() {
  local start_count
  local end_count
  local block
  local health_line
  local narrative_line

  start_count="$(grep -c '^<!-- AI-LIVE-GARDEN:STATE-START -->$' README.md || true)"
  end_count="$(grep -c '^<!-- AI-LIVE-GARDEN:STATE-END -->$' README.md || true)"

  if [[ "$start_count" != "1" || "$end_count" != "1" ]]; then
    record_violation "README.md" "Current Garden State block must have exactly one start marker and one end marker"
    return
  fi

  block="$(readme_state_block)"
  health_line="$(awk 'NF { print; exit }' <<<"$block")"
  narrative_line="$(awk '
    NF {
      seen++
      if (seen == 2) {
        print
        exit
      }
    }
  ' <<<"$block")"

  if ! grep -Eq '^\*\*Garden Health:\*\* [^[:space:]]+ (Flourishing|Stable|Strained|Critical|Dormant) — .+\.$' <<<"$health_line"; then
    record_violation "README.md" "first non-empty state-block line must be a Garden Health line with allowed status: Flourishing, Stable, Strained, Critical, or Dormant"
  fi

  if [[ -z "$narrative_line" ]]; then
    record_violation "README.md" "Current Garden State block must include a second non-empty narrative line after the health line"
  elif [[ "$narrative_line" == \#* || "$narrative_line" == -* || "$narrative_line" == '<!--'* ]]; then
    record_violation "README.md" "second non-empty state-block line must be a narrative sentence, not a heading, list item, or comment"
  fi
}

if changed_path "agent/state.md"; then
  record_violation "agent/state.md" "every autonomous run must update current project memory in place"
fi

if changed_path "README.md"; then
  record_violation "README.md" "every autonomous run must update the protected Current Garden State block"
fi

validate_readme_state_block

if (( failed != 0 )); then
  echo "Required mutable memory updates are missing or malformed."
  exit 1
fi

echo "Required mutable memory files were updated and formatted."
