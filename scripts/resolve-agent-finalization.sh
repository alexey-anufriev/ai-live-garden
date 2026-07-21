#!/usr/bin/env bash
set -euo pipefail

required=(
  SNAPSHOT_HANDOFF_OUTCOME
  AUTO_MEMORY_OUTCOME
  SYNC_JOURNAL_OUTCOME
  REQUIRED_MEMORY_OUTCOME
  JOURNAL_FORMAT_OUTCOME
  SUMMARY_FORMAT_OUTCOME
  SUMMARY_APPEND_ONLY_OUTCOME
  ARCHIVE_JOURNAL_OUTCOME
  ARCHIVE_SUMMARIES_OUTCOME
  AGENT_WORKTREE_OUTCOME
)
failures=()

for name in "${required[@]}"; do
  if [[ "${!name:-}" != "success" ]]; then
    failures+=("${name}=${!name:-skipped}")
  fi
done

if [[ "${AGENT_RUN_MODE:-}" == "evolution" ]]; then
  for name in PREPARE_VERDICT_OUTCOME RECORD_VERDICT_OUTCOME; do
    if [[ "${!name:-}" != "success" ]]; then
      failures+=("${name}=${!name:-skipped}")
    fi
  done
fi

if [[ "${AGENT_ADVANCE_GARDEN:-false}" == "true" && "${ADVANCE_GARDEN_OUTCOME:-}" != "success" ]]; then
  failures+=("ADVANCE_GARDEN_OUTCOME=${ADVANCE_GARDEN_OUTCOME:-skipped}")
fi

if [[ "${AGENT_WORKTREE_SEVERITY:-}" != "clean" ]]; then
  failures+=("AGENT_WORKTREE_SEVERITY=${AGENT_WORKTREE_SEVERITY:-missing}")
fi

if (( ${#failures[@]} == 0 )); then
  complete="true"
  reason="complete"
else
  complete="false"
  reason="$(IFS=,; echo "${failures[*]}")"
fi

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "complete=${complete}"
    echo "reason=${reason}"
  } >> "$GITHUB_OUTPUT"
fi

echo "Accepted-candidate finalization: complete=${complete}; ${reason}."
