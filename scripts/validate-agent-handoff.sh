#!/usr/bin/env bash
set -euo pipefail

handoff_file="${1:-.agent-run.json}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to parse ${handoff_file}." >&2
  exit 1
fi

if [[ ! -f "$handoff_file" ]]; then
  echo "Missing required agent handoff file: ${handoff_file}" >&2
  echo "The Gemini step completed without writing the machine-readable handoff required by the autonomous-run contract." >&2
  exit 1
fi

if ! jq -e '
  . as $root |
  type == "object" and
  (["title", "task", "why", "summary", "observations", "next", "expectedGardenEffect"] |
    all(.[]; ($root[.] | type == "string" and length > 0))) and
  (($root.requests // []) | type == "array") and
  (all(($root.requests // [])[]; type == "object")) and
  (($root.codeMap // []) | type == "array") and
  (all(($root.codeMap // [])[];
    type == "object" and
    ((.path // "") | type == "string" and length > 0) and
    ((.description // "") | type == "string" and length > 0)
  )) and
  (($root.state // {}) | type == "object") and
  (($root.state.immediateDirections // []) | type == "array") and
  (($root.state.constraints // []) | type == "array")
' "$handoff_file" >/dev/null; then
  echo "Agent handoff JSON is malformed: ${handoff_file}" >&2
  echo "Expected the exact .agent-run.json object shape described in the autonomous prompt." >&2
  exit 1
fi

echo "Agent handoff JSON is present and valid: ${handoff_file}"
