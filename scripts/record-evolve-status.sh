#!/usr/bin/env bash
set -euo pipefail

mkdir -p agent

{
  echo "# Last Autonomous Workflow Run"
  echo
  echo "- UTC: $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
  echo "- Workflow run: ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"
  echo "- Baseline test outcome: ${BASELINE_TEST_OUTCOME}"
  echo "- Gemini outcome: ${GEMINI_OUTCOME}"
  echo "- Post-change test outcome: ${POST_TEST_OUTCOME}"
  echo "- Garden state advance outcome: ${ADVANCE_GARDEN_OUTCOME}"
} > agent/last-run.md
