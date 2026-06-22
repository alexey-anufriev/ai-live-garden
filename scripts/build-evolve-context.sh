#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

output_file="$1"
mkdir -p "$(dirname "$output_file")"

recent_journal_limit="${EVOLVE_CONTEXT_RECENT_JOURNAL_LIMIT:-8}"
context_warn_lines="${EVOLVE_CONTEXT_WARN_LINES:-1200}"
metadata_file="${EVOLVE_CONTEXT_METADATA_FILE:-${output_file}.metadata}"
mkdir -p "$(dirname "$metadata_file")"

latest_files() {
  local limit="$1"
  shift
  find "$@" -maxdepth 1 -type f ! -name ".gitkeep" -print 2>/dev/null | sort -V | tail -n "$limit"
}

count_files() {
  find "$@" -maxdepth 1 -type f ! -name ".gitkeep" -print 2>/dev/null | wc -l | tr -d '[:space:]'
}

join_lines() {
  paste -sd ',' - | sed 's/,/, /g'
}

append_full_file() {
  local path="$1"

  echo "### ${path}"
  echo
  echo '```markdown'
  sed -n '1,$p' "$path"
  echo '```'
  echo
}

append_latest_summary() {
  local title="$1"
  local dir="$2"
  local file

  file="$(latest_files 1 "$dir" || true)"
  echo "## ${title}"
  echo
  if [[ -n "$file" ]]; then
    append_full_file "$file"
  else
    echo "No active ${title,,} file found."
  fi
  echo
}

append_context_manifest() {
  local journal_count

  journal_count="$(count_files "agent/journal")"

  echo "## Context Manifest"
  echo
  echo "- Full authoritative templates included: journal entry and daily summary."
  echo "- Full current memory included: \`agent/state.md\` and \`agent/requests.md\`."
  echo "- Latest active summary included per cadence: daily, weekly, monthly, yearly when present."
  echo "- Recent active journal files included fully: latest ${recent_journal_limit} of ${journal_count} directly under \`agent/journal/\`."
  echo "- Persistent garden state included as a computed digest, not as raw organism lines."
  echo "- Project source is included as a file index only; inspect exact source files only when needed for the chosen task."
  echo "- Archive folders are intentionally excluded."
  echo
}

append_garden_digest() {
  echo "## Persistent Garden State Digest"
  echo
  echo "Source: \`data/garden-state.txt\`. Use this digest first; inspect the raw state only if the chosen task requires exact organism lines."
  echo
  echo "### Environment"
  echo
  awk -F= '
    /^#/ { next }
    /^$/ { exit }
    /^[^=]+=.*$/ {
      printf "- %s: %s\n", $1, $2
    }
  ' data/garden-state.txt
  echo
  echo "### Organism Counts"
  echo
  awk -F'[=|]' '
    function numeric(value) {
      return value ~ /^-?[0-9]+([.][0-9]+)?$/
    }
    function numeric_label(fieldIndex) {
      return "numeric-field-" (fieldIndex - 3) " (organism column " fieldIndex ")"
    }
    /^organism=/ {
      total++
      type=$3
      count[type]++
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (!numeric($fieldIndex)) continue
        key=type SUBSEP numeric_label(fieldIndex)
        value=$fieldIndex + 0
        numericCount[key]++
        if (!(key in min) || value < min[key]) min[key] = value
        if (!(key in max) || value > max[key]) max[key] = value
        sum[key] += value
      }
    }
    END {
      printf "- Total organisms: %d\n", total
      for (type in count) {
        printf "- %s: %d\n", type, count[type]
      }
      for (key in numericCount) {
        split(key, parts, SUBSEP)
        type=parts[1]
        label=parts[2]
        printf "- %s %s min/max/avg: %s/%s/%.1f\n", type, label, min[key], max[key], sum[key] / numericCount[key]
      }
    }
  ' data/garden-state.txt | sort
  echo
  echo "### Most Common Traits"
  echo
  awk -F'[=|]' '
    function numeric(value) {
      return value ~ /^-?[0-9]+([.][0-9]+)?$/
    }
    /^organism=/ {
      traits=""
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (!numeric($fieldIndex)) {
          traits = traits (traits == "" ? "" : "|") $fieldIndex
        }
      }
      gsub(/\\,/, ",", traits)
      n=split(traits, parts, ",")
      for (i = 1; i <= n; i++) {
        if (parts[i] != "") trait[parts[i]]++
      }
    }
    END {
      for (name in trait) print trait[name] "\t" name
    }
  ' data/garden-state.txt | sort -nr | awk -F'\t' 'NR <= 20 { printf "- %s: %s\n", $2, $1 }'
  echo
  echo "### Attribute Extremes"
  echo
  echo "Current numeric organism fields:"
  awk -F'[=|]' '
    function numeric(value) {
      return value ~ /^-?[0-9]+([.][0-9]+)?$/
    }
    function numeric_label(fieldIndex) {
      return "numeric-field-" (fieldIndex - 3) " (organism column " fieldIndex ")"
    }
    function trait_text(    fieldIndex, text) {
      text = ""
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (!numeric($fieldIndex)) {
          text = text (text == "" ? "" : "|") $fieldIndex
        }
      }
      gsub(/\\,/, ",", text)
      return text
    }
    function other_numeric_text(skipFieldIndex,    fieldIndex, text) {
      text = ""
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (fieldIndex != skipFieldIndex && numeric($fieldIndex)) {
          text = text (text == "" ? "" : " ") numeric_label(fieldIndex) "=" $fieldIndex
        }
      }
      return text
    }
    /^organism=/ {
      traits = trait_text()
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (numeric($fieldIndex)) {
          printf "%s\t%s\t%s\t%s\t%s nonNumeric=%s\n", numeric_label(fieldIndex), $fieldIndex, $2, $3, other_numeric_text(fieldIndex), traits
        }
      }
    }
  ' data/garden-state.txt | sort -t $'\t' -k1,1 -k2,2n | awk -F'\t' '
    $1 != current {
      current=$1
      count=0
      printf "- Lowest %s:\n", current
    }
    count < 3 {
      printf "  - %s (%s): %s=%s, %s\n", $3, $4, $1, $2, $5
      count++
    }
  '
  awk -F'[=|]' '
    function numeric(value) {
      return value ~ /^-?[0-9]+([.][0-9]+)?$/
    }
    function numeric_label(fieldIndex) {
      return "numeric-field-" (fieldIndex - 3) " (organism column " fieldIndex ")"
    }
    function trait_text(    fieldIndex, text) {
      text = ""
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (!numeric($fieldIndex)) {
          text = text (text == "" ? "" : "|") $fieldIndex
        }
      }
      gsub(/\\,/, ",", text)
      return text
    }
    function other_numeric_text(skipFieldIndex,    fieldIndex, text) {
      text = ""
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (fieldIndex != skipFieldIndex && numeric($fieldIndex)) {
          text = text (text == "" ? "" : " ") numeric_label(fieldIndex) "=" $fieldIndex
        }
      }
      return text
    }
    /^organism=/ {
      traits = trait_text()
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (numeric($fieldIndex)) {
          printf "%s\t%s\t%s\t%s\t%s nonNumeric=%s\n", numeric_label(fieldIndex), $fieldIndex, $2, $3, other_numeric_text(fieldIndex), traits
        }
      }
    }
  ' data/garden-state.txt | sort -t $'\t' -k1,1 -k2,2nr | awk -F'\t' '
    $1 != current {
      current=$1
      count=0
      printf "- Highest %s:\n", current
    }
    count < 3 {
      printf "  - %s (%s): %s=%s, %s\n", $3, $4, $1, $2, $5
      count++
    }
  '
  echo
  echo "Common trait-like tokens by numeric field:"
  awk -F'[=|]' '
    function numeric(value) {
      return value ~ /^-?[0-9]+([.][0-9]+)?$/
    }
    function numeric_label(fieldIndex) {
      return "numeric-field-" (fieldIndex - 3) " (organism column " fieldIndex ")"
    }
    /^organism=/ {
      id=$2
      type=$3
      traits=""
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (!numeric($fieldIndex)) {
          traits = traits (traits == "" ? "" : "|") $fieldIndex
        }
      }
      gsub(/\\,/, ",", traits)
      n=split(traits, parts, ",")
      for (i = 1; i <= n; i++) {
        traitName=parts[i]
        if (traitName == "") continue
        count[traitName]++
        for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
          if (!numeric($fieldIndex)) continue
          key=traitName SUBSEP numeric_label(fieldIndex)
          value=$fieldIndex + 0
          if (!(key in minValue) || value < minValue[key]) {
            minValue[key]=value
            minCarrier[key]=id " (" type ")"
          }
          if (!(key in maxValue) || value > maxValue[key]) {
            maxValue[key]=value
            maxCarrier[key]=id " (" type ")"
          }
        }
      }
    }
    END {
      for (key in minValue) {
        split(key, parts, SUBSEP)
        traitName=parts[1]
        label=parts[2]
        printf "%d\t%s\t%s\t%s\t%s\t%s\t%s\n", count[traitName], traitName, label, minValue[key], minCarrier[key], maxValue[key], maxCarrier[key]
      }
    }
  ' data/garden-state.txt | sort -nr | awk -F'\t' 'NR <= 30 { printf "- %s / %s: count %s, lowest %s at %s, highest %s at %s\n", $2, $3, $1, $4, $5, $6, $7 }'
  echo
}

{
  echo "# AI Live Garden Compact Evolve Context"
  echo
  echo "Generated at: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo
  echo "You are performing one autonomous evolution step for this repository. This compact bundle is the normal context; inspect raw files only when needed for the chosen task."
  echo
  echo "## Non-Negotiable Run Contract"
  echo
  echo "- Choose exactly one small, coherent next step."
  echo "- Prefer continuity over novelty and ecological depth over disconnected additions."
  echo "- A focused new file is acceptable when it is the cleanest design."
  echo "- If a change touches simulation behavior, add or update a focused test unless the change is purely documentary or too small to test meaningfully."
  echo "- Run \`mvn test\` if possible and report the real result."
  echo "- Do not fabricate large arbitrary edits to \`data/garden-state.txt\`; normal evolution happens by simulation."
  echo "- Update \`agent/state.md\` as compact current memory, not as a run log."
  echo "- Update only the protected current-state block in \`README.md\`."
  echo "- Add one new journal entry by copying \`agent/templates/journal-entry.md\` and replacing placeholders only."
  echo "- Append exactly one current daily summary entry. Existing summary content must remain a byte-for-byte prefix."
  echo "- Update weekly/monthly/yearly summaries only when scheduled by the rules."
  echo "- Do not modify \`AGENTS.md\`, \`GEMINI.md\`, \`.github/\`, \`story/\`, license files, secrets, or prior journal entries."
  echo "- Do not read \`agent/journal/archive/\` or summary archive folders during a normal run."
  echo "- Do not ask the human what to do next."
  echo
  append_context_manifest
  echo "## Authoritative Templates"
  echo
  append_full_file "agent/templates/journal-entry.md"
  append_full_file "agent/templates/daily-summary.md"
  echo "For weekly, monthly, and yearly rollups, use the matching template in \`agent/templates/\` only when that rollup is due."
  echo
  echo "## Current Agent Memory"
  echo
  append_full_file "agent/state.md"
  echo "## Open Agent Requests"
  echo
  append_full_file "agent/requests.md"
  append_latest_summary "Latest Daily Summary" "agent/summaries/daily"
  append_latest_summary "Latest Weekly Summary" "agent/summaries/weekly"
  append_latest_summary "Latest Monthly Summary" "agent/summaries/monthly"
  append_latest_summary "Latest Yearly Summary" "agent/summaries/yearly"
  append_garden_digest
  echo "## Recent Active Journal Entries"
  echo
  while IFS= read -r journal; do
    append_full_file "$journal"
  done < <(latest_files "$recent_journal_limit" "agent/journal")
  echo "## Project Index"
  echo
  echo "### Main Java Files"
  echo
  find src/main/java -type f | sort | sed 's/^/- `/' | sed 's/$/`/'
  echo
  echo "### Test Java Files"
  echo
  find src/test/java -type f | sort | sed 's/^/- `/' | sed 's/$/`/'
  echo
  echo "### Useful Commands"
  echo
  echo "- \`mvn test\`"
  echo "- \`mvn -B -q exec:java -Dexec.args=\"inspect\"\`"
  echo "- \`mvn -B -q exec:java -Dexec.args=\"tick --steps 1\"\`"
  echo
  echo "## Final Reminder"
  echo
  echo "Make the garden slightly more expressive, coherent, observable, or maintainable than before. Leave the repository in a committable state."
} > "$output_file"

context_line_count="$(wc -l < "$output_file" | tr -d '[:space:]')"
context_byte_count="$(wc -c < "$output_file" | tr -d '[:space:]')"
if (( context_line_count > context_warn_lines )); then
  echo "Warning: compact evolve context is ${context_line_count} lines, above EVOLVE_CONTEXT_WARN_LINES=${context_warn_lines}." >&2
fi

{
  echo "EVOLVE_CONTEXT_FILE=${output_file}"
  echo "EVOLVE_CONTEXT_LINES=${context_line_count}"
  echo "EVOLVE_CONTEXT_BYTES=${context_byte_count}"
  echo "EVOLVE_CONTEXT_WARN_LINES=${context_warn_lines}"
  if (( context_line_count > context_warn_lines )); then
    echo "EVOLVE_CONTEXT_WARNED=true"
  else
    echo "EVOLVE_CONTEXT_WARNED=false"
  fi
  echo "EVOLVE_CONTEXT_RECENT_JOURNAL_LIMIT=${recent_journal_limit}"
  echo "EVOLVE_CONTEXT_ACTIVE_JOURNAL_COUNT=$(count_files "agent/journal")"
  echo "EVOLVE_CONTEXT_SELECTED_JOURNALS=$(latest_files "$recent_journal_limit" "agent/journal" | join_lines)"
  echo "EVOLVE_CONTEXT_LATEST_DAILY_SUMMARY=$(latest_files 1 "agent/summaries/daily" || true)"
  echo "EVOLVE_CONTEXT_LATEST_WEEKLY_SUMMARY=$(latest_files 1 "agent/summaries/weekly" || true)"
  echo "EVOLVE_CONTEXT_LATEST_MONTHLY_SUMMARY=$(latest_files 1 "agent/summaries/monthly" || true)"
  echo "EVOLVE_CONTEXT_LATEST_YEARLY_SUMMARY=$(latest_files 1 "agent/summaries/yearly" || true)"
} > "$metadata_file"
