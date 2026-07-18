#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

required_commands=(awk cat date find git grep jq mkdir paste sed sort tail tr wc xargs)
for required_command in "${required_commands[@]}"; do
  if ! command -v "$required_command" >/dev/null 2>&1; then
    echo "Agent context generation requires command: ${required_command}" >&2
    exit 127
  fi
done

output_file="$1"
mkdir -p "$(dirname "$output_file")"

recent_journal_limit="${AGENT_CONTEXT_RECENT_JOURNAL_LIMIT:-3}"
context_warn_lines="${AGENT_CONTEXT_WARN_LINES:-1200}"
metadata_file="${AGENT_CONTEXT_METADATA_FILE:-${output_file}.metadata}"
baseline_test_result_file="${AGENT_BASELINE_TEST_RESULT_FILE:-}"
baseline_policy_result_file="${AGENT_BASELINE_POLICY_RESULT_FILE:-}"
baseline_shadow_file="${AGENT_BASELINE_SHADOW_FILE:-}"
shadow_feedback_file="${AGENT_SHADOW_FEEDBACK_FILE:-agent/shadow-feedback.md}"
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
  echo "- Continuity sources include \`agent/state.md\`, \`agent/code-map.md\`, \`agent/requests.md\`, the newest dated \`agent/plans/YYYY-MM-DD.md\` file when present, latest active summaries, and recent active journal files."
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

append_baseline_shadow_result() {
  echo "## Baseline Shadow Simulation"
  echo
  if [[ -z "$baseline_shadow_file" || ! -f "$baseline_shadow_file" ]]; then
    echo "No baseline shadow simulation was provided."
    echo
    return
  fi

  if ! jq -e '
    type == "array" and length > 0 and
    all(.[];
      (.seed | type == "number") and
      (.status | type == "string") and
      (.final | type == "object") and
      (.final.counts | type == "object")
    )
  ' "$baseline_shadow_file" >/dev/null; then
    echo "The baseline shadow simulation file is malformed; do not claim a shadow-verified ecological target."
    echo
    return
  fi

  echo "CI will compare candidate code with these exact same-state, same-seed results. An isolated unit test is not evidence that the declared metric changes in this window."
  echo
  echo "| Seed | Steps | Status | Nutrients | Buffer | Total | Moss | Root | Spore | Fern | Fungus | Beetle | Hare | Fox | Maximum total |"
  echo "| ---: | ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |"
  jq -r '.[] | [
    .seed,
    .completedSteps,
    .status,
    .final.nutrients,
    .final.nutrientBuffer,
    .final.total,
    (.final.counts.MOSS // 0),
    (.final.counts.ROOT_NETWORK // 0),
    (.final.counts.SPORE // 0),
    (.final.counts.FERN // 0),
    (.final.counts.FUNGUS // 0),
    (.final.counts.BEETLE // 0),
    (.final.counts.HARE // 0),
    (.final.counts.FOX // 0),
    .maximumTotal
  ] | "| " + (map(tostring) | join(" | ")) + " |"' "$baseline_shadow_file"
  echo
  echo "Before finalizing a normal run: write the proposed evaluation into \`.agent-run.json\`, run \`scripts/run-maven-tests-with-timeout.sh\`, then run \`SHADOW_EVALUATION_RESULT_FILE=target/agent-preflight-result.json scripts/evaluate-shadow-candidate.sh target/agent-baseline-shadow.json .agent-run.json target/agent-candidate-shadow.json\`. Finish only when that exact preflight passes. If it fails, revise the implementation or choose a truthful metric supported by the intended behavior; do not weaken the target merely to pass."
  echo
}

append_shadow_feedback() {
  echo "## Previous Rejected Shadow Hypothesis"
  echo
  if [[ -f "$shadow_feedback_file" ]]; then
    echo "This feedback is the highest-priority continuity evidence from the previous autonomous attempt. Diagnose its missed causal link before selecting work, and do not repeat the same hypothesis unchanged."
    echo
    sed -n '1,$p' "$shadow_feedback_file"
  else
    echo "No deferred shadow rejection is awaiting consumption."
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

append_recent_implementation_pattern() {
  local commits=()
  local commit
  local source_file
  declare -A source_count=()
  local repetition_lines=()

  mapfile -t commits < <(autonomous_commits 5)

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
  done

  echo
  echo "### Repeated Source Files"
  echo
  for source_file in "${!source_count[@]}"; do
    if (( source_count["$source_file"] >= 2 )); then
      repetition_lines+=("- Source repeated in ${source_count["$source_file"]} of the last ${#commits[@]} coding runs: \`${source_file}\`.")
    fi
  done

  if (( ${#repetition_lines[@]} > 0 )); then
    printf '%s\n' "${repetition_lines[@]}" | sort
  else
    echo "- No source file was repeated in multiple recent coding runs."
  fi
  echo
}

append_project_manager_direction() {
  local latest_plan
  local active_plan
  latest_plan="$(find agent/plans -maxdepth 1 -type f -name '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].md' -print 2>/dev/null | sort -V | tail -n 1)"
  active_plan="$(scripts/find-active-agent-plan.sh)"

  echo "## Project Manager Direction"
  echo
  if [[ -n "$active_plan" ]]; then
    echo "Active highest product priority for normal autonomous runs. Baseline Maven or worktree-policy repair is the only higher priority. If repair is not required, choose exactly one listed PM direction, set \`pmDirection\` in \`.agent-run.json\` to its label, and do not invent unrelated work."
    echo
    echo "Source: \`${active_plan}\`."
    echo
    echo '```markdown'
    sed -n '1,220p' "$active_plan"
    echo '```'
  elif [[ -n "$latest_plan" ]]; then
    echo "The newest PM plan, \`${latest_plan}\`, is outside the active same-day window (maximum age \`${AGENT_PM_PLAN_MAX_AGE_DAYS:-0}\` day(s)) and is continuity context only. It is not binding because its state signals may no longer describe the living garden. Choose one focused bottleneck-first improvement from current state and set \`pmDirection\` to \`none\`."
  else
    echo "No active Project Manager direction exists. Choose one focused bottleneck-first improvement from current state and set \`pmDirection\` to \`none\`."
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
  echo "- Unless this run is repairing a failing baseline, the task must change future simulation behavior, rendered garden state, or persisted state semantics in a way that can be observed in future ticks."
  echo "- Prefer continuity over novelty and ecological depth over disconnected additions."
  echo "- Choose by expected garden value: continuity, expressive behavior, understandable state, resilience, ecological recovery, and observable consequences over time."
  echo "- Prefer changes whose value remains visible beyond the current run by making the garden more coherent, alive, inspectable, or responsive to its state."
  echo "- Prefer changes that increase future autonomous development capacity through behavior: expressive state transitions, visible consequences in future ticks, reusable simulation concepts that affect outcomes, or mechanics that create new ecological possibilities."
  echo "- Do not choose a task merely because it is the easiest way to satisfy validators. Memory, journal, summaries, tests, and validators support autonomy; they are not the purpose of the run."
  echo "- Do not repeat the recent implementation pattern by default; use the recent commit list and source-file repetition signals as context, then choose the highest-value task."
  echo "- Treat a PM direction as an outcome target, not as proof of its suggested causal mechanism. Inspect the current state and relevant code before deciding how to achieve it."
  echo "- When Ecological Outcome History reports stagnation, use a bottleneck-first change: reproduce the blocker from the current persisted population, identify the active gate, and fix that gate with a focused behavior test. Do not add or tune another named trait unless current organisms carry it or the change includes a credible adoption path."
  echo "- A passing unit test proves the modeled rule, not impact on the living state. Report both current-state evidence and the behavioral verification in the handoff."
  echo "- Declare one measurable shadow target in \`evaluation\`. CI replays baseline and candidate code from the same state and seeds; a missed target, role extinction, runaway population, or timeout discards the candidate before the real tick and records the evidence for the next run."
  echo "- Use the Baseline Shadow Simulation section as the acceptance baseline. Before finalizing, run the exact candidate preflight command shown there and report its observed delta in \`evidence.verification\`."
  echo "- You have god-mode recovery authority when persisted state causes runaway growth, timeouts, corruption, or prevents autonomous recovery. You may deterministically rebalance, cull, reseed, migrate, or directly repair \`data/garden-state.txt\`; prefer the program's \`recover\` command, preserve ecological roles, add an explanatory event, and report before/after counts."
  echo "- Never wait indefinitely for a random event or population outcome. Tests and diagnostics must be bounded. Use \`scripts/run-maven-tests-with-timeout.sh\`; if it interrupts Maven, treat the timeout as the baseline defect and replace long loops with deterministic phase-level tests."
  echo "- If the Baseline Maven Test Result says \`failed\`, repairing the existing Java source or tests is the run's required first task. Do not add unrelated behavior until \`mvn test\` passes."
  echo "- If the Baseline Worktree Policy Result says \`deferred-repair\`, repairing those policy violations is the run's required first task. Do not add unrelated behavior until the policy violations are cleared."
  echo "- If an active Project Manager Direction exists and no baseline repair is required, choose exactly one PM direction A-D as the run's highest product priority. Otherwise set \`pmDirection\` to \`none\`."
  echo "- The run is pre-approved. Do not enter planning mode, do not ask for confirmation, and do not stop after proposing a strategy."
  echo "- Choose a task for its garden value, and let implementation structure follow from that task."
  echo "- \`expectedGardenEffect\` should explain what future garden behavior or runtime state should change."
  echo "- A run that only adds a named trait, diagnostic field, renderer line, event-log message, counter, or test coverage is low value unless it directly changes future garden behavior or removes a current obstacle to ecological recovery."
  echo "- If a candidate change would only add a name, counter, log line, renderer phrase, or isolated test, choose a stronger task."
  echo "- Prefer outcome-changing work: make an overfull nutrient buffer change release behavior under drought or starvation; make missing roles recover based on current population pressure; make fungi, moss, roots, predators, or herbivores respond to measurable environmental stress; make reproduction, starvation, predation, or decay depend on existing garden state in a new observable way; or convert existing observations into behavior that affects future ticks."
  echo "- Tests are supporting evidence for meaningful behavior, not a substitute for garden value."
  echo "- Focused does not mean tiny. A bounded medium improvement may span several files when it has one clear behavioral purpose and leaves the project coherent."
  echo "- Good bounded medium tasks include making a missing ecological role recoverable from the current state, making nutrient pressure affect survival or reproduction, making state-format evolution explicit when it changes persisted simulation meaning, or introducing a resource-flow concept that directly changes future tick outcomes."
  echo "- A focused new file is acceptable when it is the cleanest design."
  echo "- Keep scope tight: change only files needed for the chosen task."
  echo "- Do not edit unrelated tests or behavior, and do not replace an existing unrelated test with a new one."
  echo "- If a change touches simulation behavior, add or update a focused test unless the change is purely documentary or too narrow to test meaningfully."
  echo "- Do not choose a tests-only task unless it exposes an important current uncertainty and the handoff explains why no behavior change was appropriate."
  echo "- Tests must prove the behavior itself, not just a log line or wording change."
  echo "- Do not weaken assertions to make tests pass or leave uncertainty comments in tests such as \"maybe\", \"wait\", \"not sure\", or \"does not distinguish\"."
  echo "- Run \`scripts/run-maven-tests-with-timeout.sh\` if possible and leave the code in a testable state."
  echo "- Normal evolution happens by simulation. During a demonstrated operability emergency, use the smallest deterministic god-mode state intervention needed and pair it with a recurrence guard."
  echo "- Do not edit generated memory, deferred feedback, or PM direction files: \`README.md\`, \`agent/state.md\`, \`agent/requests.md\`, \`agent/code-map.md\`, \`agent/shadow-feedback.md\`, \`agent/journal/\`, \`agent/summaries/\`, \`agent/templates/\`, or \`agent/plans/\`."
  echo "- Do not run memory harness scripts. CI post-processing will generate README state, code map, summaries, journal, and current memory from the final diff and garden state."
  echo "- Write \`.agent-run.json\` as the machine-readable handoff described below. This file is required."
  echo "- A handoff is not a completed run by itself. Before CI advances the garden, the worktree must contain a substantive agent-authored change under \`src/main/\`, \`src/test/\`, \`pom.xml\`, or an emergency change to \`data/garden-state.txt\`."
  echo "- Also include the same JSON object in your final response between \`AGENT_RUN_JSON_START\` and \`AGENT_RUN_JSON_END\` markers so the harness can recover the handoff if direct file creation fails."
  echo "- Before finishing, compare the final diff to the chosen task and remove accidental edits, scratch files, speculative comments, and unrelated assertion changes."
  echo "- Do not modify \`AGENTS.md\`, \`GEMINI.md\`, \`.github/\`, \`story/\`, license files, secrets, or prior journal entries."
  echo "- Do not read \`agent/journal/archive/\` or summary archive folders during a normal run."
  echo "- Do not ask the human what to do next."
  echo
  append_context_manifest
  append_recent_implementation_pattern
  scripts/report-complexity-budget.sh
  echo
  scripts/write-garden-outcome-history.sh
  echo
  append_project_manager_direction
  append_shadow_feedback
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
  "pmDirection": "A, B, C, D, or none when no PM plan exists or this run is required repair.",
  "evidence": {
    "bottleneck": "The concrete current-state gate or missing causal link this run addressed.",
    "currentState": "Evidence from data/garden-state.txt or a shadow copy showing why the change can affect living organisms.",
    "verification": "The focused test or command that proves the changed behavior, including the observed result."
  },
  "evaluation": {
    "metric": "population.BEETLE, population.FOX, population.FUNGUS, population.ROOT_NETWORK, another population type, totalOrganisms, nutrients, nutrientBuffer, or tests for required repair only",
    "goal": "increase, decrease, preserve, or pass for required repair only",
    "requiredDelta": 1
  },
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
  append_baseline_shadow_result
  append_garden_digest
  echo "## Recent Active Journal Entries"
  echo
  while IFS= read -r journal; do
    append_compact_journal_entry "$journal"
  done < <(latest_files "$recent_journal_limit" "agent/journal")
  echo "### Useful Commands"
  echo
  echo "- \`scripts/run-maven-tests-with-timeout.sh\`"
  echo "- \`mvn -B -q exec:java -Dexec.args=\"inspect\"\`"
  echo "- \`mvn -B -q exec:java -Dexec.args=\"simulate --steps 5 --seed 17 --max-organisms 25000\"\` (read-only shadow run)"
  echo "- \`mvn -B -q exec:java -Dexec.args=\"tick --steps 1\"\`"
  echo "- \`mvn -B -q exec:java -Dexec.args=\"recover --max-organisms 12000\"\` (emergency only)"
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
  echo "AGENT_CONTEXT_BASELINE_SHADOW_FILE=${baseline_shadow_file}"
  echo "AGENT_CONTEXT_SHADOW_FEEDBACK_FILE=${shadow_feedback_file}"
} > "$metadata_file"
