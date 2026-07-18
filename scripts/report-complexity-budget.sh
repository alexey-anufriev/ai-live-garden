#!/usr/bin/env bash
set -euo pipefail

source_line_budget="${AGENT_SOURCE_FILE_LINE_BUDGET:-600}"
test_ratio_budget="${AGENT_TEST_TO_SOURCE_RATIO_BUDGET:-10}"
trait_literal_budget="${AGENT_TRAIT_LITERAL_BUDGET:-150}"

source_count="$(find src/main/java -type f -name '*.java' | wc -l | tr -d '[:space:]')"
test_count="$(find src/test/java -type f -name '*.java' | wc -l | tr -d '[:space:]')"
trait_literals="$(rg -o '"[a-z][a-z0-9-]+"' src/main/java 2>/dev/null | sed 's/.*://; s/"//g' | sort -u | wc -l | tr -d '[:space:]')"
over_budget_files="$(find src/main/java -type f -name '*.java' -print0 | xargs -0 wc -l | awk -v budget="$source_line_budget" '$2 != "total" && $1 > budget { print $1 " " $2 }')"

echo "## Architecture Budget"
echo
echo "- Production Java files: ${source_count}."
echo "- Test Java files: ${test_count}."
echo "- Test/source file ratio: ${test_count}/${source_count} (advisory budget ${test_ratio_budget}:1)."
echo "- Distinct trait-like literals: ${trait_literals} (advisory budget ${trait_literal_budget})."
if [[ -n "$over_budget_files" ]]; then
  while read -r lines path; do
    echo "- Source hotspot: \`${path}\` has ${lines} lines (advisory budget ${source_line_budget})."
  done <<< "$over_budget_files"
else
  echo "- No production source file exceeds ${source_line_budget} lines."
fi

ratio_exceeded=false
if (( source_count > 0 && test_count > source_count * test_ratio_budget )); then
  ratio_exceeded=true
fi
if [[ -n "$over_budget_files" || "$ratio_exceeded" == "true" ]] || (( trait_literals > trait_literal_budget )); then
  echo "- Budget status: exceeded. Avoid adding another isolated trait/test branch to a hotspot; bounded consolidation is eligible under the architecture-budget exception."
else
  echo "- Budget status: within advisory limits."
fi
