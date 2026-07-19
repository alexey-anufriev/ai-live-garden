#!/usr/bin/env bash
set -euo pipefail

plan_file="${1:-.project-plan.json}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to parse ${plan_file}." >&2
  exit 1
fi

if [[ ! -f "$plan_file" ]]; then
  echo "Missing required project plan file: ${plan_file}" >&2
  exit 1
fi

if ! jq -e '
  . as $root |
  type == "object" and
  (.date | type == "string" and test("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")) and
  (.review | type == "object") and
  (.review.previousPlanDate | type == "string" and length > 0) and
  (.review.overallMark | type == "string" and length > 0) and
  (.review.summary | type == "string" and length > 0) and
  (.review.results | type == "array") and
  (all(.review.results[];
    type == "object" and
    ((.label // "") | type == "string" and length > 0) and
    ((.mark // "") | type == "string" and length > 0) and
    ((.evidence // "") | type == "string" and length > 0)
  )) and
  (.thesis | type == "string" and length > 20) and
  (.stateSignals | type == "array" and length >= 1 and all(.[]; type == "string" and length > 0)) and
  (.directions | type == "array" and length == 4) and
  ([.directions[].label] == ["A", "B", "C", "D"]) and
  (all(.directions[];
    type == "object" and
    (.title | type == "string" and length > 0) and
    (.why | type == "string" and length > 0) and
    (.expectedGardenEffect | type == "string" and length > 0) and
    (.acceptanceSignal | type == "string" and length > 0) and
    (.shadowAcceptance | type == "object") and
    ((.shadowAcceptance.metric // "") | type == "string" and
      test("^(population[.](MOSS|ROOT_NETWORK|SPORE|FERN|FUNGUS|BEETLE|HARE|FOX)|totalOrganisms|nutrients|nutrientBuffer)$")) and
    ((.shadowAcceptance.goal // "") | type == "string" and test("^(increase|decrease|preserve)$")) and
    ((.shadowAcceptance.requiredDelta // -1) | type == "number" and . >= 0) and
    ((.shadowAcceptance.goal == "preserve") or (.shadowAcceptance.requiredDelta > 0)) and
    (.avoid | type == "string" and length > 0)
  )) and
  (.antiPatterns | type == "array" and length >= 1 and all(.[]; type == "string" and length > 0))
' "$plan_file" >/dev/null; then
  echo "Project plan JSON is malformed: ${plan_file}" >&2
  exit 1
fi

expected_date="${PROJECT_PLAN_EXPECTED_DATE:-$(date -u +%Y-%m-%d)}"
plan_date="$(jq -r '.date' "$plan_file")"
if [[ "$plan_date" != "$expected_date" ]]; then
  echo "Project plan date must be ${expected_date}, got ${plan_date}." >&2
  exit 1
fi

echo "Project plan JSON is present and valid: ${plan_file}"
