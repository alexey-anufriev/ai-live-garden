#!/usr/bin/env bash
set -euo pipefail

violations_file="${VALIDATE_REPAIR_VIOLATIONS_FILE:-${REQUIRED_MEMORY_VIOLATIONS_FILE:-}}"

if [[ -z "$violations_file" ]]; then
  echo "VALIDATE_REPAIR_VIOLATIONS_FILE or REQUIRED_MEMORY_VIOLATIONS_FILE is required." >&2
  exit 2
fi

: > "$violations_file"

failed=0
post_test_outcome="${POST_TEST_OUTCOME:-unknown}"
garden_advance_outcome="${GARDEN_ADVANCE_OUTCOME:-${ADVANCE_GARDEN_OUTCOME:-unknown}}"
readme_change_required=true

if [[ "$post_test_outcome" == "failure" || "$garden_advance_outcome" == "skipped" ]]; then
  readme_change_required=false
fi

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

section_block() {
  local heading="$1"

  awk -v heading="$heading" '
    $0 == heading {
      in_section = 1
      next
    }
    in_section && /^### / {
      exit
    }
    in_section {
      print
    }
  ' <<<"$block"
}

validate_readme_state_block() {
  local start_count
  local end_count
  local block
  local health_line
  local second_line
  local organisms_section
  local garden_section

  start_count="$(grep -c '^<!-- AI-LIVE-GARDEN:STATE-START -->$' README.md || true)"
  end_count="$(grep -c '^<!-- AI-LIVE-GARDEN:STATE-END -->$' README.md || true)"

  if [[ "$start_count" != "1" || "$end_count" != "1" ]]; then
    record_violation "README.md" "Current Garden State block must have exactly one start marker and one end marker"
    return
  fi

  block="$(readme_state_block)"
  organisms_section="$(section_block "### Organisms")"
  garden_section="$(section_block "### Garden Characteristics")"
  health_line="$(awk 'NF { print; exit }' <<<"$block")"
  second_line="$(awk '
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

  if [[ -z "$second_line" ]]; then
    record_violation "README.md" "Current Garden State block must include structured state details after the health line"
  elif [[ "$second_line" == '<!--'* ]]; then
    record_violation "README.md" "state-block details must not begin with a comment"
  fi

  for required_heading in "### Organisms" "### Garden Characteristics"; do
    if ! grep -qx "$required_heading" <<<"$block"; then
      record_violation "README.md" "Current Garden State block must include '${required_heading}'"
    fi
  done

  if grep -qx '### Recent Trends' <<<"$block"; then
    record_violation "README.md" "trend charts must live in their respective sections, not in a separate Recent Trends section"
  fi

  if ! grep -Eq '^- Total: [0-9]+' <<<"$block"; then
    record_violation "README.md" "Organisms section must include total organism count"
  fi

  if ! grep -Eq '^- Nutrients: ' <<<"$block" || ! grep -Eq '^- Nutrient buffer: ' <<<"$block"; then
    record_violation "README.md" "Garden Characteristics must include nutrient values"
  fi

  if grep -q 'sampled' <<<"$block"; then
    record_violation "README.md" "README state block must not describe chart sampling details"
  fi

  if ! grep -Fxq '![Garden trends](agent/garden-trends.svg)' <<<"$garden_section"; then
    record_violation "README.md" "Garden Characteristics section must embed agent/garden-trends.svg"
  fi

  if ! grep -Fxq '![Organism trends](agent/organism-trends.svg)' <<<"$organisms_section"; then
    record_violation "README.md" "Organisms section must embed agent/organism-trends.svg"
  fi

  if [[ ! -f agent/garden-trends.svg ]]; then
    record_violation "agent/garden-trends.svg" "README trend chart must exist as a generated SVG"
  elif ! grep -q '<svg ' agent/garden-trends.svg; then
    record_violation "agent/garden-trends.svg" "generated trend chart must be an SVG document"
  elif grep -q 'sampled' agent/garden-trends.svg; then
    record_violation "agent/garden-trends.svg" "generated garden trend chart must not describe chart sampling details"
  elif ! grep -q '>Garden Trends<' agent/garden-trends.svg; then
    record_violation "agent/garden-trends.svg" "generated garden trend chart must describe garden trends"
  elif grep -q '>Total organisms<' agent/garden-trends.svg; then
    record_violation "agent/garden-trends.svg" "generated garden trend chart must not include organism rows"
  elif grep -q '>max ' agent/garden-trends.svg || grep -q ', min ' agent/garden-trends.svg; then
    record_violation "agent/garden-trends.svg" "generated garden trend chart must use positional axis values, not max/min text"
  elif ! grep -q 'class="axis-value"' agent/garden-trends.svg; then
    record_violation "agent/garden-trends.svg" "generated garden trend chart must show positional Y-axis values"
  fi

  if [[ ! -f agent/organism-trends.svg ]]; then
    record_violation "agent/organism-trends.svg" "README organism trend chart must exist as a generated SVG"
  elif ! grep -q '<svg ' agent/organism-trends.svg; then
    record_violation "agent/organism-trends.svg" "generated organism trend chart must be an SVG document"
  elif grep -q 'sampled' agent/organism-trends.svg; then
    record_violation "agent/organism-trends.svg" "generated organism trend chart must not describe chart sampling details"
  elif ! grep -q '>Organism Trends<' agent/organism-trends.svg; then
    record_violation "agent/organism-trends.svg" "generated organism trend chart must describe organism trends"
  elif ! grep -q '>Total organisms<' agent/organism-trends.svg; then
    record_violation "agent/organism-trends.svg" "generated organism trend chart must include total organism trend"
  elif grep -q '>max ' agent/organism-trends.svg || grep -q ', min ' agent/organism-trends.svg; then
    record_violation "agent/organism-trends.svg" "generated organism trend chart must use positional axis values, not max/min text"
  elif ! grep -q 'class="axis-value"' agent/organism-trends.svg; then
    record_violation "agent/organism-trends.svg" "generated organism trend chart must show positional Y-axis values"
  fi
}

if changed_path "agent/state.md"; then
  record_violation "agent/state.md" "every autonomous run must update current project memory in place"
fi

if [[ "$readme_change_required" == "true" ]] && changed_path "README.md"; then
  record_violation "README.md" "every autonomous run must update the protected Current Garden State block"
fi

validate_readme_state_block

if (( failed != 0 )); then
  echo "Required mutable memory updates are missing or malformed."
  exit 1
fi

echo "Required mutable memory files were updated and formatted."
