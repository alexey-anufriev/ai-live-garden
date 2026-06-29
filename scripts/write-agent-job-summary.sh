#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${GITHUB_STEP_SUMMARY:-}" ]]; then
  echo "GITHUB_STEP_SUMMARY is not set." >&2
  exit 2
fi

value_or_dash() {
  local value="${1:-}"
  if [[ -z "$value" ]]; then
    echo "-"
  else
    echo "$value"
  fi
}

metadata_value() {
  local key="$1"
  local file="${AGENT_CONTEXT_METADATA_FILE:-}"

  if [[ -z "$file" || ! -f "$file" ]]; then
    echo "-"
    return
  fi

  local line
  line="$(grep -E "^${key}=" "$file" | tail -n 1 || true)"
  if [[ -z "$line" ]]; then
    echo "-"
  else
    echo "${line#*=}"
  fi
}

{
  echo "## Agent Workflow Summary"
  echo
  echo "- UTC: $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
  echo "- Run: ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"
  echo "- Attempt: ${GITHUB_RUN_ATTEMPT:-1}"
  echo "- Gemini execution model: $(value_or_dash "${EXECUTION_MODEL:-}")"
  echo
  echo "| Phase | Outcome |"
  echo "| --- | --- |"
  echo "| Baseline test | $(value_or_dash "${BASELINE_TEST_OUTCOME:-}") |"
  echo "| Gemini autonomous step | $(value_or_dash "${GEMINI_OUTCOME:-}") |"
  echo "| Protected file restore | $(value_or_dash "${RESTORE_PROTECTED_OUTCOME:-}") |"
  echo "| Post-Gemini test validation | $(value_or_dash "${POST_TEST_OUTCOME:-}") |"
  echo "| Garden state advance | $(value_or_dash "${ADVANCE_GARDEN_OUTCOME:-}") |"
  echo "| Automated memory generation | $(value_or_dash "${AUTO_MEMORY_OUTCOME:-}") |"
  echo "| Required memory validation | $(value_or_dash "${REQUIRED_MEMORY_OUTCOME:-}") |"
  echo "| Journal format validation | $(value_or_dash "${JOURNAL_FORMAT_OUTCOME:-}") |"
  echo "| Summary format validation | $(value_or_dash "${SUMMARY_FORMAT_OUTCOME:-}") |"
  echo "| Summary append-only validation | $(value_or_dash "${SUMMARY_APPEND_ONLY_OUTCOME:-}") |"
  echo "| Journal archive | $(value_or_dash "${ARCHIVE_JOURNAL_OUTCOME:-}") |"
  echo "| Summary archive | $(value_or_dash "${ARCHIVE_SUMMARIES_OUTCOME:-}") |"
  echo "| Agent worktree validation | $(value_or_dash "${AGENT_WORKTREE_OUTCOME:-}") |"
  echo "| Failure diagnostics collection | $(value_or_dash "${COLLECT_DIAGNOSTICS_OUTCOME:-}") |"
  echo "| Failure diagnostics upload | $(value_or_dash "${UPLOAD_DIAGNOSTICS_OUTCOME:-}") |"
  echo "| Workflow status record | $(value_or_dash "${RECORD_STATUS_OUTCOME:-}") |"
  echo "| Change diagnostics collection | $(value_or_dash "${COLLECT_CHANGE_DIAGNOSTICS_OUTCOME:-}") |"
  echo "| Commit | $(value_or_dash "${COMMIT_OUTCOME:-}") |"
  echo
  echo "## Context Diagnostics"
  echo
  echo "- Context lines: $(metadata_value AGENT_CONTEXT_LINES)"
  echo "- Context bytes: $(metadata_value AGENT_CONTEXT_BYTES)"
  echo "- Warning threshold: $(metadata_value AGENT_CONTEXT_WARN_LINES)"
  echo "- Warning threshold exceeded: $(metadata_value AGENT_CONTEXT_WARNED)"
  echo "- Recent journal limit: $(metadata_value AGENT_CONTEXT_RECENT_JOURNAL_LIMIT)"
  echo "- Active journal files: $(metadata_value AGENT_CONTEXT_ACTIVE_JOURNAL_COUNT)"
  echo "- Selected journal files: $(value_or_dash "$(metadata_value AGENT_CONTEXT_SELECTED_JOURNALS)")"
  echo "- Latest daily summary: $(value_or_dash "$(metadata_value AGENT_CONTEXT_LATEST_DAILY_SUMMARY)")"
  echo "- Latest weekly summary: $(value_or_dash "$(metadata_value AGENT_CONTEXT_LATEST_WEEKLY_SUMMARY)")"
  echo "- Latest monthly summary: $(value_or_dash "$(metadata_value AGENT_CONTEXT_LATEST_MONTHLY_SUMMARY)")"
  echo "- Latest yearly summary: $(value_or_dash "$(metadata_value AGENT_CONTEXT_LATEST_YEARLY_SUMMARY)")"
  echo
  if [[ -n "${AGENT_CHANGE_DIAGNOSTICS_FILE:-}" && -f "$AGENT_CHANGE_DIAGNOSTICS_FILE" ]]; then
    cat "$AGENT_CHANGE_DIAGNOSTICS_FILE"
    echo
  fi
  if [[ "${COLLECT_DIAGNOSTICS_OUTCOME:-}" == "success" || "${UPLOAD_DIAGNOSTICS_OUTCOME:-}" == "success" ]]; then
    echo "Failure diagnostics artifact: \`agent-failure-diagnostics-${GITHUB_RUN_ATTEMPT:-1}\`."
  elif [[ "${GEMINI_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed during the main Gemini call. This is usually provider quota, authentication, or model availability."
  elif [[ "${POST_TEST_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed after Gemini while validating tests. No automatic Gemini repair loop is run; a committed failing baseline is surfaced to the next agent through the prompt."
  elif [[ "${AUTO_MEMORY_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while generating deterministic memory artifacts."
  elif [[ "${SUMMARY_APPEND_ONLY_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while validating append-only summaries."
  elif [[ "${REQUIRED_MEMORY_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while validating required README/state memory updates."
  elif [[ "${JOURNAL_FORMAT_OUTCOME:-}" == "failure" || "${SUMMARY_FORMAT_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while validating generated memory format."
  elif [[ "${AGENT_WORKTREE_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed because the final worktree contained generated artifacts, scratch files, or weak test patterns."
  elif [[ "${COMMIT_OUTCOME:-}" == "success" ]]; then
    echo "The run reached the commit step."
  else
    echo "No specific failure hint was detected from recorded step outcomes."
  fi
} >> "$GITHUB_STEP_SUMMARY"
