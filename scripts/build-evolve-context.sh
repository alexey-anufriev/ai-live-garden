#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

output_file="$1"
mkdir -p "$(dirname "$output_file")"

latest_files() {
  local limit="$1"
  shift
  find "$@" -maxdepth 1 -type f ! -name ".gitkeep" -print 2>/dev/null | sort -V | tail -n "$limit"
}

append_file_excerpt() {
  local path="$1"
  local max_lines="$2"

  echo "### ${path}"
  echo
  echo '```markdown'
  sed -n "1,${max_lines}p" "$path"
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
    echo "Source: \`${file}\`"
    echo
    echo '```markdown'
    tail -n 80 "$file"
    echo '```'
  else
    echo "No active ${title,,} file found."
  fi
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
    /^(version|cycle|nextId|light|moisture|warmth|nutrients|nutrientBuffer)=/ {
      printf "- %s: %s\n", $1, $2
    }
  ' data/garden-state.txt
  echo
  echo "### Organism Counts"
  echo
  awk -F'[=|]' '
    /^organism=/ {
      total++
      type=$3
      count[type]++
      energy=$4 + 0
      if (!(type in min) || energy < min[type]) min[type] = energy
      if (!(type in max) || energy > max[type]) max[type] = energy
      sum[type] += energy
      if (type == "BEETLE" || type == "HARE" || type == "FOX") animals++
      else plants++
    }
    END {
      printf "- Total organisms: %d\n", total
      printf "- Plants and fungi/root networks: %d\n", plants
      printf "- Animals: %d\n", animals
      for (type in count) {
        printf "- %s: %d, energy min/max/avg: %d/%d/%.1f\n", type, count[type], min[type], max[type], sum[type] / count[type]
      }
    }
  ' data/garden-state.txt | sort
  echo
  echo "### Most Common Traits"
  echo
  awk -F'[=|]' '
    /^organism=/ {
      traits=$7
      gsub(/\\,/, ",", traits)
      n=split(traits, parts, ",")
      for (i = 1; i <= n; i++) {
        if (parts[i] != "") trait[parts[i]]++
      }
    }
    END {
      for (name in trait) print trait[name] "\t" name
    }
  ' data/garden-state.txt | sort -nr | head -20 | awk -F'\t' '{ printf "- %s: %s\n", $2, $1 }'
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
  echo "## Authoritative Templates"
  echo
  append_file_excerpt "agent/templates/journal-entry.md" 80
  append_file_excerpt "agent/templates/daily-summary.md" 60
  echo "For weekly, monthly, and yearly rollups, use the matching template in \`agent/templates/\` only when that rollup is due."
  echo
  echo "## Current Agent Memory"
  echo
  append_file_excerpt "agent/state.md" 140
  echo "## Open Agent Requests"
  echo
  append_file_excerpt "agent/requests.md" 120
  append_latest_summary "Latest Daily Summary" "agent/summaries/daily"
  append_latest_summary "Latest Weekly Summary" "agent/summaries/weekly"
  append_latest_summary "Latest Monthly Summary" "agent/summaries/monthly"
  append_latest_summary "Latest Yearly Summary" "agent/summaries/yearly"
  append_garden_digest
  echo "## Recent Active Journal Entries"
  echo
  while IFS= read -r journal; do
    append_file_excerpt "$journal" 80
  done < <(latest_files 8 "agent/journal")
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
