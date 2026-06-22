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

{
  echo "## Evolve Workflow Summary"
  echo
  echo "- UTC: $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
  echo "- Run: ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"
  echo "- Attempt: ${GITHUB_RUN_ATTEMPT:-1}"
  echo "- Model: $(value_or_dash "${GEMINI_MODEL:-}")"
  echo
  echo "| Phase | Outcome |"
  echo "| --- | --- |"
  echo "| Baseline test | $(value_or_dash "${BASELINE_TEST_OUTCOME:-}") |"
  echo "| Gemini autonomous step | $(value_or_dash "${GEMINI_OUTCOME:-}") |"
  echo "| Protected file restore | $(value_or_dash "${RESTORE_PROTECTED_OUTCOME:-}") |"
  echo "| Post-Gemini test validation | $(value_or_dash "${POST_TEST_OUTCOME:-}") |"
  echo "| Garden state advance | $(value_or_dash "${ADVANCE_GARDEN_OUTCOME:-}") |"
  echo "| Journal format validation | $(value_or_dash "${JOURNAL_FORMAT_OUTCOME:-}") |"
  echo "| Summary format validation | $(value_or_dash "${SUMMARY_FORMAT_OUTCOME:-}") |"
  echo "| Summary append-only validation | $(value_or_dash "${SUMMARY_APPEND_ONLY_OUTCOME:-}") |"
  echo "| Journal archive | $(value_or_dash "${ARCHIVE_JOURNAL_OUTCOME:-}") |"
  echo "| Summary archive | $(value_or_dash "${ARCHIVE_SUMMARIES_OUTCOME:-}") |"
  echo "| Failure diagnostics collection | $(value_or_dash "${COLLECT_DIAGNOSTICS_OUTCOME:-}") |"
  echo "| Failure diagnostics upload | $(value_or_dash "${UPLOAD_DIAGNOSTICS_OUTCOME:-}") |"
  echo "| Workflow status record | $(value_or_dash "${RECORD_STATUS_OUTCOME:-}") |"
  echo "| Commit | $(value_or_dash "${COMMIT_OUTCOME:-}") |"
  echo
  if [[ "${COLLECT_DIAGNOSTICS_OUTCOME:-}" == "success" || "${UPLOAD_DIAGNOSTICS_OUTCOME:-}" == "success" ]]; then
    echo "Failure diagnostics artifact: \`evolve-failure-diagnostics-${GITHUB_RUN_ATTEMPT:-1}\`."
  elif [[ "${GEMINI_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed during the main Gemini call. This is usually provider quota, authentication, or model availability."
  elif [[ "${POST_TEST_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed after Gemini while validating or repairing tests."
  elif [[ "${SUMMARY_APPEND_ONLY_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while validating or repairing append-only summaries."
  elif [[ "${JOURNAL_FORMAT_OUTCOME:-}" == "failure" || "${SUMMARY_FORMAT_OUTCOME:-}" == "failure" ]]; then
    echo "The run failed while validating or repairing generated memory format."
  elif [[ "${COMMIT_OUTCOME:-}" == "success" ]]; then
    echo "The run reached the commit step."
  else
    echo "No specific failure hint was detected from recorded step outcomes."
  fi
} >> "$GITHUB_STEP_SUMMARY"
