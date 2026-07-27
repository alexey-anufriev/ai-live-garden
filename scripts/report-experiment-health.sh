#!/usr/bin/env bash
set -euo pipefail

limit="${1:-20}"
if ! [[ "$limit" =~ ^[1-9][0-9]*$ ]]; then
  echo "Usage: $0 [POSITIVE_LIMIT]" >&2
  exit 2
fi

records_file="$(mktemp)"
trap 'rm -f "$records_file"' EXIT
count=0
while IFS= read -r commit; do
  lineage="$(git show "${commit}:agent/shadow-feedback.md" 2>/dev/null | awk '
    /^<!-- AGENT-EXPERIMENT-LINEAGE-START -->$/ { capture = 1; next }
    /^<!-- AGENT-EXPERIMENT-LINEAGE-END -->$/ { exit }
    capture && !/^```/ { print }
  ' || true)"
  [[ -n "$lineage" ]] || continue
  if jq -c --arg commit "$commit" '.current + {commit:$commit}' <<<"$lineage" >> "$records_file" 2>/dev/null; then
    count=$((count + 1))
    (( count >= limit )) && break
  fi
done < <(git log --format='%H' -n $((limit * 6)) -- agent/shadow-feedback.md)

echo "# Autonomous Experiment Health"
echo
echo "Recent committed verdicts examined: $(wc -l < "$records_file" | tr -d '[:space:]') (limit ${limit})"
echo
if [[ ! -s "$records_file" ]]; then
  echo "No committed experiment verdicts found."
  exit 0
fi
jq -s '
  group_by(.classification) |
  map({classification: .[0].classification, count: length}) |
  sort_by(.classification) |
  .[] | "- \(.classification): \(.count)"
' "$records_file"
echo
echo "Run this report after several autonomous runs to calibrate thresholds; it is human-facing and does not direct or reject an individual candidate."
