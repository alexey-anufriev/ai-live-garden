#!/usr/bin/env bash
set -euo pipefail

mkdir -p agent

{
  echo "# Last Autonomous Workflow Run"
  echo
  echo "- UTC: $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
  echo "- Workflow run: ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"
  echo "- Baseline test outcome: ${BASELINE_TEST_OUTCOME}"
  echo "- Baseline worktree policy outcome: ${BASELINE_POLICY_OUTCOME:-}"
  echo "- Observation window outcome: ${OBSERVATION_WINDOW_OUTCOME:-}"
  echo "- Gemini outcome: ${GEMINI_OUTCOME}"
  echo "- Incomplete agent feedback outcome: ${AGENT_INCOMPLETE_FEEDBACK_OUTCOME:-}"
  echo "- Agent handoff extraction outcome: ${EXTRACT_AGENT_HANDOFF_OUTCOME:-}"
  echo "- Agent handoff outcome: ${AGENT_HANDOFF_OUTCOME:-}"
  echo "- Post-change test outcome: ${POST_TEST_OUTCOME}"
  echo "- Shadow evaluation outcome: ${SHADOW_EVALUATION_OUTCOME:-}"
  echo "- Shadow operability repair outcome: ${SHADOW_REPAIR_EVALUATION_OUTCOME:-}"
  echo "- Deferred shadow feedback outcome: ${SHADOW_FEEDBACK_OUTCOME:-}"
  echo "- Garden state advance outcome: ${ADVANCE_GARDEN_OUTCOME}"
  echo "- Automated memory outcome: ${AUTO_MEMORY_OUTCOME}"
  echo "- Required memory outcome: ${REQUIRED_MEMORY_OUTCOME}"
  echo "- Journal format outcome: ${JOURNAL_FORMAT_OUTCOME}"
  echo "- Summary format outcome: ${SUMMARY_FORMAT_OUTCOME}"
  echo "- Summary append-only outcome: ${SUMMARY_APPEND_ONLY_OUTCOME}"
  echo "- Agent worktree validation outcome: ${AGENT_WORKTREE_OUTCOME:-}"
  echo "- Agent worktree repair-input severity: ${AGENT_WORKTREE_REPAIR_INPUT_SEVERITY:-}"
  echo "- Agent worktree final severity: ${AGENT_WORKTREE_SEVERITY:-}"
} > agent/last-run.md
