#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

output_file="$1"
mkdir -p "$(dirname "$output_file")"

recent_journal_limit="${AGENT_CONTEXT_RECENT_JOURNAL_LIMIT:-3}"
context_warn_lines="${AGENT_CONTEXT_WARN_LINES:-1200}"
metadata_file="${AGENT_CONTEXT_METADATA_FILE:-${output_file}.metadata}"
baseline_test_result_file="${AGENT_BASELINE_TEST_RESULT_FILE:-}"
baseline_policy_result_file="${AGENT_BASELINE_POLICY_RESULT_FILE:-}"
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

append_requests() {
  local path="agent/requests.md"

  if awk '
    /^## Open requests/ { in_open = 1; next }
    /^## / && in_open { in_open = 0 }
    in_open && /\*No open requests yet\.\*/ { empty = 1 }
    END { exit empty ? 0 : 1 }
  ' "$path"; then
    echo "No open requests."
    echo
    return
  fi

  append_full_file "$path"
}

append_summary_entries() {
  local title="$1"
  local dir="$2"
  local entry_limit="$3"
  local file

  file="$(latest_files 1 "$dir" || true)"
  echo "## ${title}"
  echo
  if [[ -n "$file" ]]; then
    echo '```markdown'
    awk -v limit="$entry_limit" '
      /^### / {
        entry_count++
      }
      {
        lines[NR] = $0
        entry_at_line[NR] = entry_count
      }
      END {
        first_entry = entry_count - limit + 1
        if (first_entry < 1) {
          first_entry = 1
        }
        for (line = 1; line <= NR; line++) {
          if (entry_at_line[line] >= first_entry) {
            print lines[line]
          }
        }
      }
    ' "$file"
    echo '```'
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
  echo "- README state, summaries, journal, and current memory are generated after the agent step by scripts."
  echo "- Continuity sources include \`agent/state.md\`, \`agent/code-map.md\`, \`agent/requests.md\`, latest active summaries, and recent active journal files."
  echo "- Recent active journal source set: latest ${recent_journal_limit} of ${journal_count} directly under \`agent/journal/\`."
  echo "- Persistent garden state is included as an exact computed digest, not as raw organism lines."
  echo "- Project source is summarized in \`agent/code-map.md\`; inspect exact source files only when needed for the chosen task."
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
    /^organism=/ {
      total++
      count[$3]++
    }
    END {
      printf "- Total organisms: %d\n", total
      for (type in count) {
        printf "- %s: %d\n", type, count[type]
      }
    }
  ' data/garden-state.txt
  echo
  echo "### Attribute Extremes"
  echo
  awk -F'[=|]' '
    function numeric(value) {
      return value ~ /^-?[0-9]+([.][0-9]+)?$/
    }
    function numeric_label(fieldIndex) {
      return "numeric-field-" (fieldIndex - 3) " (organism column " fieldIndex ")"
    }
    /^organism=/ {
      for (fieldIndex = 4; fieldIndex <= NF; fieldIndex++) {
        if (numeric($fieldIndex)) {
          label = numeric_label(fieldIndex)
          value = $fieldIndex + 0
          count[label]++
          if (!(label in min) || value < min[label]) {
            min[label] = value
            minOrganism[label] = $2
            minType[label] = $3
          }
          if (!(label in max) || value > max[label]) {
            max[label] = value
            maxOrganism[label] = $2
            maxType[label] = $3
          }
          sum[label] += value
        }
      }
    }
    END {
      for (label in count) {
        printf "- %s: min %s at %s (%s), max %s at %s (%s), avg %.1f\n", label, min[label], minOrganism[label], minType[label], max[label], maxOrganism[label], maxType[label], sum[label] / count[label]
      }
    }
  ' data/garden-state.txt | sort
  echo
}

append_baseline_test_result() {
  echo "## Baseline Maven Test Result"
  echo
  if [[ -n "$baseline_test_result_file" && -f "$baseline_test_result_file" ]]; then
    sed -n '1,$p' "$baseline_test_result_file"
  else
    echo "No baseline Maven test result file was provided."
  fi
  echo
}

append_baseline_policy_result() {
  echo "## Baseline Worktree Policy Result"
  echo
  if [[ -n "$baseline_policy_result_file" && -f "$baseline_policy_result_file" ]]; then
    sed -n '1,$p' "$baseline_policy_result_file"
  else
    echo "No baseline worktree policy result file was provided."
  fi
  echo
}

autonomous_commits() {
  git log --grep='^feat: autonomous garden evolution' --format='%H' -n "${1:-3}" 2>/dev/null || true
}

commit_title() {
  local commit="$1"
  git show -s --format='%s' "$commit"
}

commit_source_files() {
  local commit="$1"
  git show --name-only --format='' "$commit" 2>/dev/null |
    sed '/^$/d' |
    awk '/^src\/main\/java\/.+[.]java$/ { print }' |
    sort -u
}

commit_domain_terms() {
  local commit="$1"
  {
    commit_title "$commit"
    git show --name-only --format='' "$commit" 2>/dev/null |
      awk '/^agent\/journal\/[^/]+[.]md$/ { print }' |
      while IFS= read -r journal; do
        [[ -n "$journal" ]] || continue
        git show "${commit}:${journal}" 2>/dev/null | sed -n '1,24p' || true
      done
  } |
    tr '[:upper:]' '[:lower:]' |
    awk '
      {
        while (match($0, /(fungal|fungus|nutrient|buffer|trait|observability|recovery|succession|predator|herbivore|root|moss|renderer)/)) {
          print substr($0, RSTART, RLENGTH)
          $0 = substr($0, RSTART + RLENGTH)
        }
      }
    ' |
    sort -u
}

append_recent_implementation_pattern() {
  local commits=()
  local commit
  local source_file
  local term
  declare -A source_count=()
  declare -A term_count=()
  local repetition_lines=()

  mapfile -t commits < <(autonomous_commits 3)

  echo "## Recent Autonomous Coding Pattern"
  echo
  if (( ${#commits[@]} == 0 )); then
    echo "No recent autonomous coding commits found."
    echo
    return
  fi

  for commit in "${commits[@]}"; do
    echo "- $(git show -s --format='%h %cd %s' --date=short "$commit")"
    while IFS= read -r source_file; do
      [[ -n "$source_file" ]] || continue
      source_count["$source_file"]=$(( ${source_count["$source_file"]:-0} + 1 ))
    done < <(commit_source_files "$commit")
    while IFS= read -r term; do
      [[ -n "$term" ]] || continue
      term_count["$term"]=$(( ${term_count["$term"]:-0} + 1 ))
    done < <(commit_domain_terms "$commit")
  done

  echo
  echo "### Repetition Signals"
  echo
  for source_file in "${!source_count[@]}"; do
    if (( source_count["$source_file"] >= 2 )); then
      repetition_lines+=("- Source repeated in ${source_count["$source_file"]} of the last ${#commits[@]} coding runs: \`${source_file}\`.")
    fi
  done
  for term in "${!term_count[@]}"; do
    if (( term_count["$term"] >= 2 )); then
      repetition_lines+=("- Domain term repeated in ${term_count["$term"]} of the last ${#commits[@]} coding runs: ${term}.")
    fi
  done

  if (( ${#repetition_lines[@]} > 0 )); then
    printf '%s\n' "${repetition_lines[@]}" | sort
    echo "- When recent work repeats the same file or domain term, prefer consolidation, simplification, or a different high-value area over another narrow exception."
  else
    echo "- No strong repetition signal detected."
  fi
  echo
}

append_compact_journal_entry() {
  local path="$1"

  echo '```markdown'
  awk '
    /^## Files changed$/ { skip = 1; next }
    /^## Checks run$/ { skip = 1; next }
    /^## Result of `mvn test`$/ { skip = 1; next }
    /^## / { skip = 0 }
    !skip { print }
  ' "$path"
  echo '```'
  echo
}

{
  echo "# AI Live Garden Compact Agent Context"
  echo
  echo "Generated at: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo
  echo "You are performing one focused bounded improvement for this repository. This compact bundle is the normal context; inspect raw files only when needed for the chosen task."
  echo
  echo "Important workflow model: AI agents perform functional evolution of source code, tests, rendering, and project files when needed. Separate scripts handle memory, README state, journal, summaries, and simulation ticks."
  echo
  echo "## Non-Negotiable Run Contract"
  echo
  echo "- Choose exactly one focused bounded improvement with a visible expected future effect."
  echo "- Prefer continuity over novelty and ecological depth over disconnected additions."
  echo "- Choose by expected garden value: continuity, expressive behavior, understandable state, resilience, evolvability, and observable consequences over time."
  echo "- Prefer changes whose value remains visible beyond the current run by making the garden more coherent, alive, inspectable, or able to keep evolving."
  echo "- Prefer changes that increase future autonomous development capacity: clearer domain boundaries, more expressive state transitions, visible behavioral consequences in future ticks, reusable simulation concepts, or mechanics that create new future possibilities."
  echo "- Do not choose a task merely because it is the easiest way to satisfy validators. Memory, journal, summaries, tests, and validators support autonomy; they are not the purpose of the run."
  echo "- Do not repeat the recent implementation pattern by default. If recent runs mostly added similar named mechanisms, diagnostics, event logs, counters, or tests, treat those categories as saturated."
  echo "- If the Baseline Maven Test Result says \`failed\`, repairing the existing Java source or tests is the run's required first task. Do not add unrelated behavior until \`mvn test\` passes."
  echo "- A run that only adds a named trait, diagnostic field, renderer line, event-log message, counter, or test coverage is low value unless it directly changes future garden behavior or removes a current obstacle to ecological recovery."
  echo "- If a candidate change would only add a name, counter, log line, renderer phrase, or isolated test, choose a stronger task."
  echo "- Prefer outcome-changing work: consolidate duplicate mechanics, connect existing rules into feedback loops, make missing ecological roles recover through simulation, simplify state transitions, or convert existing observations into behavior that affects future ticks."
  echo "- Tests are supporting evidence for meaningful behavior, not a substitute for garden value."
  echo "- Focused does not mean tiny. A bounded medium improvement may span several files when it has one clear behavioral purpose and leaves the project coherent."
  echo "- Good bounded medium tasks include consolidating duplicate nutrient-buffer mechanics, making a missing ecological role recoverable from the current state, simplifying survival or reproduction flow, introducing a reusable resource-flow concept that replaces duplicated code, or making state-format evolution explicit."
  echo "- A focused new file is acceptable when it is the cleanest design."
  echo "- Keep scope tight: change only files needed for the chosen task."
  echo "- Do not edit unrelated tests or behavior, and do not replace an existing unrelated test with a new one."
  echo "- If a change touches simulation behavior, add or update a focused test unless the change is purely documentary or too narrow to test meaningfully."
  echo "- Do not choose a tests-only task unless it exposes an important current uncertainty and the handoff explains why no behavior change was appropriate."
  echo "- Tests must prove the behavior itself, not just a log line or wording change."
  echo "- Do not weaken assertions to make tests pass or leave uncertainty comments in tests such as \"maybe\", \"wait\", \"not sure\", or \"does not distinguish\"."
  echo "- Run \`mvn test\` if possible and leave the code in a testable state."
  echo "- Do not fabricate large arbitrary edits to \`data/garden-state.txt\`; normal evolution happens by simulation."
  echo "- Do not edit generated memory files: \`README.md\`, \`agent/state.md\`, \`agent/requests.md\`, \`agent/code-map.md\`, \`agent/journal/\`, \`agent/summaries/\`, or \`agent/templates/\`."
  echo "- Do not run memory harness scripts. CI post-processing will generate README state, code map, summaries, journal, and current memory from the final diff and garden state."
  echo "- Write \`.agent-run.json\` as the machine-readable handoff described below. This file is required."
  echo "- Also include the same JSON object in your final response between \`AGENT_RUN_JSON_START\` and \`AGENT_RUN_JSON_END\` markers so the harness can recover the handoff if direct file creation fails."
  echo "- Before finishing, compare the final diff to the chosen task and remove accidental edits, scratch files, speculative comments, and unrelated assertion changes."
  echo "- Do not modify \`AGENTS.md\`, \`GEMINI.md\`, \`.github/\`, \`story/\`, license files, secrets, or prior journal entries."
  echo "- Do not read \`agent/journal/archive/\` or summary archive folders during a normal run."
  echo "- Do not ask the human what to do next."
  echo
  append_context_manifest
  append_recent_implementation_pattern
  echo "## Automatic Post-Processing"
  echo
  echo "After this step, CI runs \`scripts/agent-auto-postprocess.sh\`. It parses \`.agent-run.json\`, restores generated memory files, refreshes \`agent/code-map.md\`, advances documentation from \`data/garden-state.txt\`, appends summaries, creates the journal entry, applies any request entries, removes \`.agent-run.json\`, and leaves those artifacts for validation."
  echo
  echo "## Required Agent Handoff"
  echo
  echo "Before finishing, create \`.agent-run.json\` with this exact JSON object shape. Use concise strings. Use an empty \`requests\` array when no human help is needed. Then repeat the exact same JSON object in your final response between \`AGENT_RUN_JSON_START\` and \`AGENT_RUN_JSON_END\` markers."
  echo
  cat <<'JSON'
```json
{
  "title": "Short title for this run",
  "task": "One sentence describing the chosen implementation task.",
  "why": "One short paragraph explaining why this was the right next step.",
  "summary": "One short paragraph summarizing what changed.",
  "observations": "One short paragraph describing what was learned or any limitations.",
  "next": "One concrete possible next direction.",
  "expectedGardenEffect": "What future ticks should do differently because of this change.",
  "codeMap": [
    {
      "path": "src/main/java/garden/ai/FileChangedByThisRun.java",
      "description": "One short stable description of what this source file is for, not what changed in this run."
    }
  ],
  "requests": [
    {
      "title": "Short request title",
      "what": "What human help, tool, dependency, permission, or boundary change is requested.",
      "why": "Why this would help the garden evolve.",
      "affected": "Files, workflow, dependency, or configuration areas affected.",
      "benefit": "Expected benefit.",
      "fallback": "What agents can do if this is not approved."
    }
  ],
  "state": {
    "immediateDirections": [
      "Optional current direction for future runs."
    ],
    "constraints": [
      "Optional active constraint or known bad idea."
    ]
  }
}
```
JSON
  echo
  echo "Do not wrap the actual \`.agent-run.json\` file in Markdown fences. Do not wrap the final marked JSON block in Markdown fences either."
  echo
  echo "## Current Agent Memory"
  echo
  append_full_file "agent/state.md"
  echo "## Code Map"
  echo
  append_full_file "agent/code-map.md"
  echo "## Open Agent Requests"
  echo
  append_requests
  append_summary_entries "Latest Daily Summary Entries" "agent/summaries/daily" 3
  append_summary_entries "Latest Weekly Summary Entry" "agent/summaries/weekly" 1
  append_summary_entries "Latest Monthly Summary Entry" "agent/summaries/monthly" 1
  append_baseline_test_result
  append_baseline_policy_result
  append_garden_digest
  echo "## Recent Active Journal Entries"
  echo
  while IFS= read -r journal; do
    append_compact_journal_entry "$journal"
  done < <(latest_files "$recent_journal_limit" "agent/journal")
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
  echo "AGENT_CONTEXT_BASELINE_TEST_RESULT_FILE=${baseline_test_result_file}"
  echo "AGENT_CONTEXT_BASELINE_POLICY_RESULT_FILE=${baseline_policy_result_file}"
} > "$metadata_file"
