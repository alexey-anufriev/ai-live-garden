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
  echo "- Gemini CLI version: $(value_or_dash "${GEMINI_CLI_VERSION:-}")"
  echo
  echo "| Phase | Outcome |"
  echo "| --- | --- |"
  echo "| Baseline test | $(value_or_dash "${BASELINE_TEST_OUTCOME:-}") |"
  echo "| Baseline shadow simulation | $(value_or_dash "${BASELINE_SHADOW_OUTCOME:-}") |"
  echo "| Baseline worktree policy | $(value_or_dash "${BASELINE_POLICY_OUTCOME:-}") |"
  echo "| Observation window | $(value_or_dash "${OBSERVATION_WINDOW_OUTCOME:-}") |"
  echo "| Gemini autonomous step | $(value_or_dash "${GEMINI_OUTCOME:-}") |"
  echo "| Protected file restore | $(value_or_dash "${RESTORE_PROTECTED_OUTCOME:-}") |"
  echo "| Gemini primary output inspection | $(value_or_dash "${GEMINI_PRIMARY_INSPECT_OUTCOME:-}") |"
  echo "| Incomplete agent feedback | $(value_or_dash "${AGENT_INCOMPLETE_FEEDBACK_OUTCOME:-}") |"
  echo "| Incomplete feedback commit | $(value_or_dash "${AGENT_FEEDBACK_COMMIT_OUTCOME:-}") |"
  echo "| Agent handoff extraction | $(value_or_dash "${EXTRACT_AGENT_HANDOFF_OUTCOME:-}") |"
  echo "| Agent handoff validation | $(value_or_dash "${AGENT_HANDOFF_OUTCOME:-}") |"
  echo "| Post-Gemini test validation | $(value_or_dash "${POST_TEST_OUTCOME:-}") |"
  echo "| Run mode / shadow policy | $(value_or_dash "${AGENT_RUN_MODE:-}") / $(value_or_dash "${AGENT_SHADOW_POLICY:-}") |"
  echo "| Candidate validation | $(value_or_dash "${CANDIDATE_VALIDATION_OUTCOME:-}") |"
  echo "| Candidate shadow evaluation | $(value_or_dash "${SHADOW_EVALUATION_OUTCOME:-}") |"
  echo "| Candidate shadow safety | $(value_or_dash "${SHADOW_SAFETY_EVALUATION_OUTCOME:-}") |"
  echo "| Shadow operability repair | $(value_or_dash "${SHADOW_REPAIR_EVALUATION_OUTCOME:-}") |"
  echo "| Rejected candidate branch publication | $(value_or_dash "${REJECTED_CANDIDATE_PUBLISH_OUTCOME:-}") |"
  echo "| Deferred shadow feedback | $(value_or_dash "${SHADOW_FEEDBACK_OUTCOME:-}") |"
  echo "| Deferred feedback commit | $(value_or_dash "${SHADOW_FEEDBACK_COMMIT_OUTCOME:-}") |"
  echo "| Rejected candidate branch cleanup | $(value_or_dash "${REJECTED_CANDIDATE_CLEANUP_OUTCOME:-}") |"
  echo "| Consumed candidate branch cleanup | $(value_or_dash "${CONSUMED_REJECTED_CANDIDATE_CLEANUP_OUTCOME:-}") |"
  echo "| Garden state advance | $(value_or_dash "${ADVANCE_GARDEN_OUTCOME:-}") |"
  echo "| Automated memory generation | $(value_or_dash "${AUTO_MEMORY_OUTCOME:-}") |"
  echo "| Required memory validation | $(value_or_dash "${REQUIRED_MEMORY_OUTCOME:-}") |"
  echo "| Journal format validation | $(value_or_dash "${JOURNAL_FORMAT_OUTCOME:-}") |"
  echo "| Summary format validation | $(value_or_dash "${SUMMARY_FORMAT_OUTCOME:-}") |"
  echo "| Summary append-only validation | $(value_or_dash "${SUMMARY_APPEND_ONLY_OUTCOME:-}") |"
  echo "| Journal archive | $(value_or_dash "${ARCHIVE_JOURNAL_OUTCOME:-}") |"
  echo "| Summary archive | $(value_or_dash "${ARCHIVE_SUMMARIES_OUTCOME:-}") |"
  echo "| Agent worktree validation | $(value_or_dash "${AGENT_WORKTREE_OUTCOME:-}") |"
  echo "| Agent worktree repair-input severity | $(value_or_dash "${AGENT_WORKTREE_REPAIR_INPUT_SEVERITY:-}") |"
  echo "| Agent worktree final severity | $(value_or_dash "${AGENT_WORKTREE_SEVERITY:-}") |"
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
  if [[ "${GEMINI_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed during the main Gemini call. This is usually provider quota, authentication, or model availability."
  elif [[ "${GEMINI_PRIMARY_INSPECT_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while inspecting Gemini output for plan-mode/no-op behavior."
  elif [[ "${AGENT_INCOMPLETE_FEEDBACK_OUTCOME:-}" == "success" ]]; then
    echo "The agent call was incomplete. Its partial changes were discarded and its evidence was committed for the next run without a same-run retry."
  elif [[ "${EXTRACT_AGENT_HANDOFF_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed because Gemini did not leave a valid \`.agent-run.json\` file or marked handoff JSON in its output."
  elif [[ "${AGENT_HANDOFF_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed because Gemini did not leave a valid \`.agent-run.json\` handoff."
  elif [[ ( "${SHADOW_EVALUATION_OUTCOME:-}" == "failure" || "${SHADOW_SAFETY_EVALUATION_OUTCOME:-}" == "failure" || "${SHADOW_REPAIR_EVALUATION_OUTCOME:-}" == "failure" ) && "${SHADOW_FEEDBACK_OUTCOME:-}" == "success" ]]; then
    echo "DEFERRED_NO_EFFECT: the candidate missed its deterministic ecological target or safety bounds. Its exact source was preserved on a rejected-candidate branch, compact evidence was committed for the next autonomous run, and the garden was not advanced."
  elif [[ "${POST_TEST_OUTCOME:-}" == "failure" ]]; then
    echo "Post-Gemini Maven validation failed. The workflow intentionally commits the failed baseline and deterministic memory so the next autonomous run starts with repair."
  elif [[ "${AUTO_MEMORY_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while generating deterministic memory artifacts."
  elif [[ "${SUMMARY_APPEND_ONLY_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while validating append-only summaries."
  elif [[ "${REQUIRED_MEMORY_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while validating required README/state memory updates."
  elif [[ "${JOURNAL_FORMAT_OUTCOME:-}" == "failure" || "${SUMMARY_FORMAT_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while validating generated memory format."
  elif [[ "${AGENT_WORKTREE_SEVERITY:-}" == "hard" ]]; then
    echo "The run failed because the final worktree contained hard policy violations that cannot be safely committed as repair input."
  elif [[ "${AGENT_WORKTREE_SEVERITY:-}" == "deferred" ]]; then
    echo "Final worktree policy found repairable source-quality violations. The workflow intentionally commits them so the next autonomous run starts with repair."
  elif [[ "${COMMIT_OUTCOME:-}" == "success" ]]; then
    echo "The run reached the commit step."
  else
    echo "No specific failure hint was detected from recorded step outcomes."
  fi

  if [[ "${COLLECT_DIAGNOSTICS_OUTCOME:-}" == "success" || "${UPLOAD_DIAGNOSTICS_OUTCOME:-}" == "success" ]]; then
    echo
    echo "Failure diagnostics artifact: \`agent-failure-diagnostics-${GITHUB_RUN_ATTEMPT:-1}\`."
  fi
} >> "$GITHUB_STEP_SUMMARY"
