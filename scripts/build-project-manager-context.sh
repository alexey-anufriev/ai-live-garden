#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

output_file="$1"
mkdir -p "$(dirname "$output_file")"

latest_plan() {
  find agent/plans -maxdepth 1 -type f -name '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].md' -print 2>/dev/null | sort -V | tail -n 1
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

append_agent_requests() {
  local path="agent/requests.md"

  if [[ -f "$path" ]]; then
    append_full_file "$path"
  else
    echo "No agent requests file found."
    echo
  fi
}

append_agent_state() {
  local path="agent/state.md"

  if [[ -f "$path" ]]; then
    append_full_file "$path"
  else
    echo "No agent state file found."
    echo
  fi
}

append_garden_digest() {
  echo "## Garden State Digest"
  echo
  echo "Source: \`data/garden-state.txt\`. Use this digest as the main source of truth. Do not inspect Java source or project code for this planning task."
  echo
  echo "### Environment"
  awk -F= '
    /^#/ { next }
    /^$/ { exit }
    /^[^=]+=.*$/ {
      printf "- %s: %s\n", $1, $2
    }
  ' data/garden-state.txt
  echo
  echo "### Organism Counts"
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
  ' data/garden-state.txt | sort
  echo
  echo "### Energy By Type"
  awk -F'[=|]' '
    /^organism=/ {
      type = $3
      energy = $4 + 0
      count[type]++
      sum[type] += energy
      if (!(type in min) || energy < min[type]) min[type] = energy
      if (!(type in max) || energy > max[type]) max[type] = energy
    }
    END {
      for (type in count) {
        printf "- %s: min %s, max %s, avg %.1f\n", type, min[type], max[type], sum[type] / count[type]
      }
    }
  ' data/garden-state.txt | sort
  echo
  echo "### Age Or Generation Column By Type"
  awk -F'[=|]' '
    /^organism=/ {
      type = $3
      value = $5 + 0
      count[type]++
      sum[type] += value
      if (!(type in min) || value < min[type]) min[type] = value
      if (!(type in max) || value > max[type]) max[type] = value
    }
    END {
      for (type in count) {
        printf "- %s: min %s, max %s, avg %.1f\n", type, min[type], max[type], sum[type] / count[type]
      }
    }
  ' data/garden-state.txt | sort
  echo
  echo "### Top Non-Ephemeral Traits"
  awk -F'[=|]' '
    /^organism=/ {
      traits = $NF
      gsub(/\\,/, "\034", traits)
      n = split(traits, parts, "\034")
      for (i = 1; i <= n; i++) {
        trait = parts[i]
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", trait)
        if (trait == "" || trait ~ /^fed-[0-9]+$/) continue
        count[trait]++
      }
    }
    END {
      for (trait in count) {
        printf "%d\t%s\n", count[trait], trait
      }
    }
  ' data/garden-state.txt | sort -rn | head -n 25 | awk -F'\t' '{ printf "- %s: %s\n", $2, $1 }'
  echo
}

append_latest_plan() {
  local plan

  plan="$(latest_plan || true)"
  echo "## Previous PM Direction"
  echo
  if [[ -n "$plan" ]]; then
    append_full_file "$plan"
  else
    echo "No previous PM direction found."
    echo
  fi
}

{
  echo "# AI Live Garden Project Manager Context"
  echo
  echo "Generated at: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo
  echo "You are the daily opportunity scout for AI Live Garden. Your job is not to edit source code. Your job is to recommend the four highest-value directions for autonomous agents to work on during the next day."
  echo
  echo "## Planning Contract"
  echo
  echo "- Use mainly the garden state digest from \`data/garden-state.txt\`, current agent state, open requests, and previous PM direction."
  echo "- Do not inspect Java source files, tests, \`agent/code-map.md\`, or workflow files. This is product/ecology planning, not code review."
  echo "- Do not propose refactoring, centralization, extraction, cleanup, renaming, or maintainability-only work."
  echo "- Each direction must describe an observable garden outcome for future ticks: population balance, nutrients, buffer pressure, survival, reproduction, predation, decay, succession, recovery, or environment response."
  echo "- Suggest directions, not implementation files. The autonomous agent will inspect code later if it chooses a direction."
  echo "- Baseline repair always takes precedence during agent runs. Once repair is clear, your directions are the highest product priority for autonomous agents that day."
  echo "- Produce exactly four directions labeled A, B, C, and D."
  echo "- Write \`.project-plan.json\` with the JSON shape below. Also include the same JSON in your final response between \`PROJECT_PLAN_JSON_START\` and \`PROJECT_PLAN_JSON_END\`."
  echo
  echo "## Required JSON Shape"
  echo
  cat <<'JSON'
```json
{
  "date": "YYYY-MM-DD",
  "thesis": "One paragraph: PM thinks A, B, C, and D will make the most progress today because...",
  "stateSignals": [
    "Garden-state signal that motivates the plan."
  ],
  "directions": [
    {
      "label": "A",
      "title": "Short direction title",
      "why": "Why this direction matters now based on garden state.",
      "expectedGardenEffect": "Observable future tick effect.",
      "acceptanceSignal": "What would show that the direction was successfully implemented.",
      "avoid": "What the agent should not do while pursuing this direction."
    }
  ],
  "antiPatterns": [
    "Pattern to avoid today."
  ]
}
```
JSON
  echo
  append_garden_digest
  echo "## Current Agent State"
  echo
  append_agent_state
  echo "## Agent Requests"
  echo
  append_agent_requests
  append_latest_plan
} > "$output_file"
