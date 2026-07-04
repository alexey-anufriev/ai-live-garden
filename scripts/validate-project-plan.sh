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
  (.thesis | type == "string" and length > 20) and
  (.stateSignals | type == "array" and length >= 3 and all(.[]; type == "string" and length > 0)) and
  (.directions | type == "array" and length == 4) and
  ([.directions[].label] == ["A", "B", "C", "D"]) and
  (all(.directions[];
    type == "object" and
    (.title | type == "string" and length > 0) and
    (.why | type == "string" and length > 0) and
    (.expectedGardenEffect | type == "string" and length > 0) and
    (.acceptanceSignal | type == "string" and length > 0) and
    (.avoid | type == "string" and length > 0)
  )) and
  (.antiPatterns | type == "array" and length >= 1 and all(.[]; type == "string" and length > 0))
' "$plan_file" >/dev/null; then
  echo "Project plan JSON is malformed: ${plan_file}" >&2
  exit 1
fi

all_plan_text="$(jq -r '[
  .thesis,
  (.stateSignals[]),
  (.directions[] | [.title, .why, .expectedGardenEffect, .acceptanceSignal, .avoid] | join("\n")),
  (.antiPatterns[])
] | join("\n") | ascii_downcase' "$plan_file")"

positive_plan_text="$(jq -r '[
  .thesis,
  (.stateSignals[]),
  (.directions[] | [.title, .why, .expectedGardenEffect, .acceptanceSignal] | join("\n"))
] | join("\n") | ascii_downcase' "$plan_file")"

if grep -Eq '(^|[^[:alnum:]_/.-])(src/|scripts/|[.][a-z0-9_-]+[.]java|pom[.]xml|traitregistry|organisminteractioncalculator|garden[.]java|github/workflows)' <<<"$all_plan_text"; then
  echo "Project plan appears to suggest code files or implementation internals." >&2
  echo "The PM plan must describe garden directions, not code targets." >&2
  exit 1
fi

if grep -Eq 'refactor|centraliz(e|ed|es|ing|ation)|consolidat(e|ed|es|ing|ion)|cleanup|clean up|rename|extract|maintainability|readability|cleaner code|technical debt' <<<"$positive_plan_text"; then
  echo "Project plan contains maintenance-only or refactoring language." >&2
  echo "The PM plan must focus on observable garden progress." >&2
  exit 1
fi

while IFS= read -r effect; do
  if ! grep -Eiq 'future tick|future ticks|tick|cycle|garden|organism|population|nutrient|buffer|starv|reproduc|predat|decay|decompos|fung|moss|root|herbivore|predator|beetle|fox|hare|plant|spore|role|surviv|energy|environment|drought|stress|recover|coloniz|succession|food chain' <<<"$effect"; then
    echo "Direction expectedGardenEffect lacks an observable garden outcome: ${effect}" >&2
    exit 1
  fi
done < <(jq -r '.directions[].expectedGardenEffect' "$plan_file")

echo "Project plan JSON is present and valid: ${plan_file}"
