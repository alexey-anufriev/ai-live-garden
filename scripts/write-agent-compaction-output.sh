#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 OUTPUT_FILE" >&2
  exit 2
fi

output_file="$1"
mkdir -p "$(dirname "$output_file")"

if [[ -z "${AGENT_CONTEXT_COMPACTION_SUMMARY:-}" ]]; then
  echo "AGENT_CONTEXT_COMPACTION_SUMMARY is empty." >&2
  exit 2
fi

printf '%s\n' "$AGENT_CONTEXT_COMPACTION_SUMMARY" > "$output_file"
