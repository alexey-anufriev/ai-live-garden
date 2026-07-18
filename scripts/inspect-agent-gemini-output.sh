#!/usr/bin/env bash
set -euo pipefail

mode="classify"
artifact_dir=""

usage() {
  cat >&2 <<'USAGE'
Usage:
  scripts/inspect-agent-gemini-output.sh [--fail-on-noop] GEMINI_ARTIFACT_DIR

Classifies Gemini CLI output for autonomous agent runs. The script uses the
structured Gemini CLI JSON stats when available, especially enter_plan_mode
tool usage, and checks whether the run left a valid handoff or repository
changes.
USAGE
  exit 2
}

while (( $# > 0 )); do
  case "$1" in
    --fail-on-noop)
      mode="fail-on-noop"
      shift
      ;;
    -h|--help)
      usage
      ;;
    -*)
      echo "Unknown argument: $1" >&2
      usage
      ;;
    *)
      if [[ -n "$artifact_dir" ]]; then
        echo "Unexpected extra argument: $1" >&2
        usage
      fi
      artifact_dir="$1"
      shift
      ;;
  esac
done

if [[ -z "$artifact_dir" ]]; then
  usage
fi

stdout_file="${artifact_dir}/stdout.log"
plan_mode_count="0"
total_tool_calls="0"
valid_handoff="false"
artifact_handoff="false"
worktree_changed="false"
substantive_change="false"
retry_required="false"
noop_reason="none"

if [[ -f "$stdout_file" ]] && jq -e . "$stdout_file" >/dev/null 2>&1; then
  plan_mode_count="$(jq -r '.stats.tools.byName.enter_plan_mode.count // 0' "$stdout_file")"
  total_tool_calls="$(jq -r '.stats.tools.totalCalls // 0' "$stdout_file")"
fi

if [[ -f .agent-run.json ]] && scripts/validate-agent-handoff.sh .agent-run.json >/dev/null 2>&1; then
  valid_handoff="true"
fi

if [[ "$valid_handoff" != "true" && -d "$artifact_dir" ]]; then
  artifact_candidate="$(mktemp)"
  rm -f "$artifact_candidate"
  if scripts/extract-agent-handoff.sh --output "$artifact_candidate" "$artifact_dir" >/dev/null 2>&1; then
    valid_handoff="true"
    artifact_handoff="true"
  fi
  rm -f "$artifact_candidate"
fi

if [[ -n "$(git status --porcelain -uall)" ]]; then
  worktree_changed="true"
fi

if scripts/agent-substantive-changes.sh >/dev/null; then
  substantive_change="true"
fi

repair_required="false"
if [[ "${AGENT_BASELINE_TEST_OUTCOME:-success}" != "success" || "${AGENT_BASELINE_POLICY_OUTCOME:-success}" != "success" || "${AGENT_BASELINE_SHADOW_OUTCOME:-success}" != "success" ]]; then
  repair_required="true"
fi
if [[ "$repair_required" == "true" ]] && git status --porcelain -uall |
    sed '/^$/d' | sed 's/^...//; s/.* -> //' |
    grep -Ev '^(\.agent-run[.]json|README[.]md|agent/(state[.]md|requests[.]md|code-map[.]md|garden-trends[.]svg|organism-trends[.]svg|journal/|summaries/|templates/|plans/)|story/)' |
    grep -q .; then
  substantive_change="true"
fi

if [[ "$valid_handoff" == "true" && "$substantive_change" != "true" ]]; then
  retry_required="true"
  noop_reason="handoff-without-substantive-change"
elif [[ "$valid_handoff" != "true" && ( "$worktree_changed" == "true" || "$plan_mode_count" != "0" || "$total_tool_calls" != "0" ) ]]; then
  retry_required="true"
  if [[ "$worktree_changed" == "true" ]]; then
    noop_reason="changes-without-valid-handoff"
  elif [[ "$plan_mode_count" != "0" ]]; then
    noop_reason="plan-mode-without-handoff-or-changes"
  else
    noop_reason="agent-activity-without-valid-handoff-or-changes"
  fi
elif [[ "$valid_handoff" != "true" ]]; then
  retry_required="true"
  noop_reason="agent-returned-no-valid-handoff-or-changes"
fi

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "plan_mode_count=${plan_mode_count}"
    echo "total_tool_calls=${total_tool_calls}"
    echo "valid_handoff=${valid_handoff}"
    echo "artifact_handoff=${artifact_handoff}"
    echo "worktree_changed=${worktree_changed}"
    echo "substantive_change=${substantive_change}"
    echo "retry_required=${retry_required}"
    echo "noop_reason=${noop_reason}"
  } >> "$GITHUB_OUTPUT"
fi

{
  echo "Gemini autonomous output inspection:"
  echo "- Artifact directory: ${artifact_dir}"
  echo "- Plan-mode tool calls: ${plan_mode_count}"
  echo "- Total tool calls: ${total_tool_calls}"
  echo "- Valid handoff available: ${valid_handoff}"
  echo "- Valid handoff recovered from artifacts: ${artifact_handoff}"
  echo "- Repository changes present: ${worktree_changed}"
  echo "- Substantive agent change present: ${substantive_change}"
  echo "- Retry required: ${retry_required}"
  echo "- Reason: ${noop_reason}"
}

if [[ "$mode" == "fail-on-noop" && "$retry_required" == "true" ]]; then
  echo "Gemini did not complete a valid autonomous run with both a handoff and a substantive change." >&2
  echo "Reason: ${noop_reason}." >&2
  exit 1
fi
