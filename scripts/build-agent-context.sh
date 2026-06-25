#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

output_file="$1"
mkdir -p "$(dirname "$output_file")"

recent_journal_limit="${AGENT_CONTEXT_RECENT_JOURNAL_LIMIT:-8}"
context_warn_lines="${AGENT_CONTEXT_WARN_LINES:-1200}"
metadata_file="${AGENT_CONTEXT_METADATA_FILE:-${output_file}.metadata}"
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
  echo "- Full authoritative templates included: journal entry, daily summary, weekly summary, monthly summary, and yearly summary."
  echo "- Continuity sources include \`agent/state.md\`, \`agent/requests.md\`, latest active summaries, and recent active journal files. In compacted workflow prompts, these may appear as a compacted continuity digest instead of full files."
  echo "- Recent active journal source set: latest ${recent_journal_limit} of ${journal_count} directly under \`agent/journal/\`."
  echo "- Persistent garden state is included as an exact computed digest, not as raw organism lines."
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
}

{
  echo "# AI Live Garden Compact Agent Context"
  echo
  echo "Generated at: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo
  echo "You are performing one autonomous evolution step for this repository. This compact bundle is the normal context; inspect raw files only when needed for the chosen task."
  echo
  echo "Important workflow model: AI agents perform functional evolution of rules, tests, rendering, memory, and documentation. Separate AI-less tick workflows may advance \`data/garden-state.txt\` by simulation only. Treat tick commits as independent ecological history, and distinguish your code/memory changes from state changes produced by simulation ticks."
  echo
  echo "## Non-Negotiable Run Contract"
  echo
  echo "- Choose exactly one small, coherent next step."
  echo "- Prefer continuity over novelty and ecological depth over disconnected additions."
  echo "- Choose by expected garden value: continuity, expressive behavior, understandable state, resilience, evolvability, and observable consequences over time."
  echo "- Prefer changes whose value remains visible beyond the current run by making the garden more coherent, alive, inspectable, or able to keep evolving."
  echo "- Prefer changes that increase future autonomous development capacity: clearer domain boundaries, more expressive state transitions, better observability of live behavior, reusable simulation concepts, or mechanics that create new future possibilities."
  echo "- Do not choose a task merely because it is the easiest way to satisfy validators. Memory, journal, summaries, tests, and validators support autonomy; they are not the purpose of the run."
  echo "- Do not repeat the recent implementation pattern by default. If recent runs mostly added similar named mechanisms, prefer consolidation, observability, clearer state transitions, or current ecosystem feedback unless a new mechanism has a tested and observable effect. Tests are supporting evidence for meaningful behavior, not a substitute for garden value."
  echo "- A focused new file is acceptable when it is the cleanest design."
  echo "- Keep scope tight: change only files needed for the chosen task plus required memory files."
  echo "- Do not edit unrelated tests or behavior, and do not replace an existing unrelated test with a new one."
  echo "- If a change touches simulation behavior, add or update a focused test unless the change is purely documentary or too small to test meaningfully."
  echo "- Do not choose a tests-only task unless it protects existing behavior future runs are likely to build on or exposes an important current uncertainty."
  echo "- Tests must prove the behavior itself, not just a log line or wording change."
  echo "- Do not weaken assertions to make tests pass or leave uncertainty comments in tests such as \"maybe\", \"wait\", \"not sure\", or \"does not distinguish\"."
  echo "- Run \`mvn test\` if possible and report the real result."
  echo "- Do not fabricate large arbitrary edits to \`data/garden-state.txt\`; normal evolution happens by simulation."
  echo "- Update \`agent/state.md\` as compact current memory, not as a run log."
  echo "- Update only the protected current-state block in \`README.md\`, grounded in the current garden snapshot and recent events."
  echo "- Do not claim absent organism categories are currently benefiting from a change; describe latent or future behavior as such."
  echo "- Add one new journal entry by copying \`agent/templates/journal-entry.md\` and replacing placeholders only."
  echo "- Append exactly one current daily summary entry. Existing summary content must remain a byte-for-byte prefix."
  echo "- Update weekly/monthly/yearly summaries only when scheduled by the rules."
  echo "- Before finishing, compare the final diff to the chosen task and remove accidental edits, scratch files, speculative comments, and unrelated assertion changes."
  echo "- Do not modify \`AGENTS.md\`, \`GEMINI.md\`, \`.github/\`, \`story/\`, license files, secrets, or prior journal entries."
  echo "- Do not read \`agent/journal/archive/\` or summary archive folders during a normal run."
  echo "- Do not ask the human what to do next."
  echo
  append_context_manifest
  echo "## Authoritative Templates"
  echo
  append_full_file "agent/templates/journal-entry.md"
  append_full_file "agent/templates/daily-summary.md"
  append_full_file "agent/templates/weekly-summary.md"
  append_full_file "agent/templates/monthly-summary.md"
  append_full_file "agent/templates/yearly-summary.md"
  echo "Use weekly, monthly, and yearly templates only when that rollup is due."
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
  echo "Warning: compact agent context is ${context_line_count} lines, above AGENT_CONTEXT_WARN_LINES=${context_warn_lines}." >&2
fi

{
  echo "AGENT_CONTEXT_FILE=${output_file}"
  echo "AGENT_CONTEXT_LINES=${context_line_count}"
  echo "AGENT_CONTEXT_BYTES=${context_byte_count}"
  echo "AGENT_CONTEXT_WARN_LINES=${context_warn_lines}"
  if (( context_line_count > context_warn_lines )); then
    echo "AGENT_CONTEXT_WARNED=true"
  else
    echo "AGENT_CONTEXT_WARNED=false"
  fi
  echo "AGENT_CONTEXT_RECENT_JOURNAL_LIMIT=${recent_journal_limit}"
  echo "AGENT_CONTEXT_ACTIVE_JOURNAL_COUNT=$(count_files "agent/journal")"
  echo "AGENT_CONTEXT_SELECTED_JOURNALS=$(latest_files "$recent_journal_limit" "agent/journal" | join_lines)"
  echo "AGENT_CONTEXT_LATEST_DAILY_SUMMARY=$(latest_files 1 "agent/summaries/daily" || true)"
  echo "AGENT_CONTEXT_LATEST_WEEKLY_SUMMARY=$(latest_files 1 "agent/summaries/weekly" || true)"
  echo "AGENT_CONTEXT_LATEST_MONTHLY_SUMMARY=$(latest_files 1 "agent/summaries/monthly" || true)"
  echo "AGENT_CONTEXT_LATEST_YEARLY_SUMMARY=$(latest_files 1 "agent/summaries/yearly" || true)"
} > "$metadata_file"
