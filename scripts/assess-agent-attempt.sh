#!/usr/bin/env bash
set -euo pipefail

if (( $# != 4 )); then
  echo "Usage: $0 ATTEMPT GEMINI_ARTIFACT_DIR BASELINE_SHADOW_FILE RESULT_FILE" >&2
  exit 2
fi

attempt="$1"
if ! [[ "$attempt" =~ ^[123]$ ]]; then
  echo "ATTEMPT must be 1, 2, or 3." >&2
  exit 2
fi
artifact_dir="$2"
baseline_shadow_file="$3"
result_file="$4"
attempt_dir="${RUNNER_TEMP:-/tmp}/agent-attempt-${attempt}"
mkdir -p "$attempt_dir" "$(dirname "$result_file")"

accepted="false"
retry_required="true"
substantive_change="false"
candidate_commit=""
stage="output"
stage_rank="0"
reason="agent-output-invalid"
diagnostics_file="$attempt_dir/diagnostics.log"
shadow_result_file="$attempt_dir/shadow-result.json"
printf 'null\n' > "$shadow_result_file"

write_result() {
  local diagnostic_text=""
  if [[ -f "$diagnostics_file" ]]; then
    diagnostic_text="$(tail -n 100 "$diagnostics_file")"
  fi
  jq -n \
    --argjson attempt "$attempt" \
    --argjson accepted "$accepted" \
    --argjson retryRequired "$retry_required" \
    --argjson substantiveChange "$substantive_change" \
    --arg candidateCommit "$candidate_commit" \
    --argjson stageRank "$stage_rank" \
    --arg stage "$stage" \
    --arg reason "$reason" \
    --arg diagnostics "$diagnostic_text" \
    --slurpfile shadow "$shadow_result_file" '
      {
        attempt: $attempt,
        accepted: $accepted,
        retryRequired: $retryRequired,
        substantiveChange: $substantiveChange,
        candidateCommit: $candidateCommit,
        stageRank: $stageRank,
        stage: $stage,
        reason: $reason,
        diagnostics: $diagnostics,
        shadow: ($shadow[0] // null)
      }
    ' > "$result_file"
  if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
    {
      echo "accepted=${accepted}"
      echo "retry_required=${retry_required}"
      echo "substantive_change=${substantive_change}"
      echo "candidate_commit=${candidate_commit}"
      echo "stage=${stage}"
      echo "reason=${reason}"
    } >> "$GITHUB_OUTPUT"
  fi
  cat "$result_file"
}

inspect_outputs="$attempt_dir/inspect.outputs"
if ! AGENT_HANDOFF_ALLOW_UNVERIFIED_PREFLIGHT=true GITHUB_OUTPUT="$inspect_outputs" \
    scripts/inspect-agent-gemini-output.sh "$artifact_dir" > "$diagnostics_file" 2>&1; then
  reason="agent-output-inspection-failed"
  write_result
  exit 0
fi

if grep -Fxq 'substantive_change=true' "$inspect_outputs"; then
  substantive_change="true"
  candidate_commit="$(scripts/snapshot-agent-candidate.sh "$attempt" 2>> "$diagnostics_file" || true)"
  if [[ -z "$candidate_commit" ]]; then
    echo "A substantive candidate could not be snapshotted for attempt ${attempt}." >&2
    cat "$diagnostics_file" >&2
    exit 1
  fi
fi
if ! grep -Fxq 'valid_handoff=true' "$inspect_outputs"; then
  reason="$(sed -n 's/^handoff_validation_reason=//p' "$inspect_outputs" | head -n 1)"
  if [[ -z "$reason" || "$reason" == "none" ]]; then
    reason="$(sed -n 's/^noop_reason=//p' "$inspect_outputs" | head -n 1)"
  fi
  [[ -n "$reason" ]] || reason="missing-valid-handoff"
  write_result
  exit 0
fi
if [[ "$substantive_change" != "true" ]]; then
  reason="handoff-without-substantive-change"
  write_result
  exit 0
fi

stage="handoff"
stage_rank="1"
if ! AGENT_HANDOFF_ALLOW_UNVERIFIED_PREFLIGHT=true \
    scripts/extract-agent-handoff.sh --output .agent-run.json "$artifact_dir" > "$diagnostics_file" 2>&1; then
  reason="handoff-extraction-failed"
  write_result
  exit 0
fi

objective_file="${RUNNER_TEMP:-/tmp}/agent-attempt-objective.json"
current_objective="$attempt_dir/objective.json"
jq '{runMode, pmDirection}' .agent-run.json > "$current_objective"
if [[ -f "$objective_file" ]]; then
  if ! jq -e --slurpfile expected "$objective_file" '. == $expected[0]' "$current_objective" >/dev/null; then
    reason="candidate-objective-changed"
    {
      echo "Bounded repairs must keep the original run mode and PM direction."
      echo "Expected: $(jq -c . "$objective_file")"
      echo "Received: $(jq -c . "$current_objective")"
    } > "$diagnostics_file"
    write_result
    exit 0
  fi
else
  cp "$current_objective" "$objective_file"
fi

stage="tests"
stage_rank="2"
test_violations="$attempt_dir/test-violations.txt"
if ! VALIDATE_REPAIR_VIOLATIONS_FILE="$test_violations" scripts/validate-tests.sh \
    > "$diagnostics_file" 2>&1; then
  reason="candidate-tests-failed"
  cp "$test_violations" "$diagnostics_file" 2>/dev/null || true
  write_result
  exit 0
fi

stage="worktree"
stage_rank="3"
worktree_outputs="$attempt_dir/worktree.outputs"
worktree_violations="$attempt_dir/worktree-violations.txt"
if ! VALIDATE_AGENT_WORKTREE_SCOPE=changed \
    VALIDATE_AGENT_WORKTREE_VIOLATIONS_FILE="$worktree_violations" \
    GITHUB_OUTPUT="$worktree_outputs" scripts/validate-agent-worktree.sh \
    > "$diagnostics_file" 2>&1; then
  reason="candidate-worktree-policy-failed"
  cp "$worktree_violations" "$diagnostics_file" 2>/dev/null || true
  write_result
  exit 0
fi

stage="policy"
stage_rank="4"
policy_outputs="$attempt_dir/policy.outputs"
if ! AGENT_HANDOFF_ALLOW_UNVERIFIED_PREFLIGHT=true GITHUB_OUTPUT="$policy_outputs" \
    scripts/derive-agent-validation-policy.sh .agent-run.json > "$diagnostics_file" 2>&1; then
  reason="candidate-validation-policy-failed"
  write_result
  exit 0
fi
shadow_policy="$(sed -n 's/^shadow_policy=//p' "$policy_outputs" | head -n 1)"

if [[ "$shadow_policy" == "target" || "$shadow_policy" == "safety" ]]; then
  stage="repeat-guard"
  stage_rank="5"
  repeated_result="$attempt_dir/repeated-result.json"
  if ! REPEATED_CANDIDATE_RESULT_FILE="$repeated_result" \
      scripts/detect-repeated-rejected-candidate.sh agent/shadow-feedback.md "$baseline_shadow_file" \
      > "$diagnostics_file" 2>&1; then
    reason="repeated-previous-rejection"
    cp "$repeated_result" "$shadow_result_file" 2>/dev/null || true
    write_result
    exit 0
  fi
fi

stage="shadow"
stage_rank="6"
case "$shadow_policy" in
  skip)
    ;;
  target)
    if ! SHADOW_EVALUATION_RESULT_FILE="$shadow_result_file" \
        scripts/evaluate-shadow-candidate.sh "$baseline_shadow_file" .agent-run.json \
          "$attempt_dir/candidate-shadow.json" > "$diagnostics_file" 2>&1; then
      if jq -e '.observedDelta | type == "number"' "$shadow_result_file" >/dev/null 2>&1; then
        scripts/sync-agent-preflight-handoff.sh "$shadow_result_file" .agent-run.json
      fi
      reason="candidate-shadow-target-missed"
      write_result
      exit 0
    fi
    scripts/sync-agent-preflight-handoff.sh "$shadow_result_file" .agent-run.json
    ;;
  safety)
    if [[ "${AGENT_BASELINE_SHADOW_OUTCOME:-success}" == "success" ]]; then
      if ! SHADOW_EVALUATION_POLICY=safety SHADOW_EVALUATION_RESULT_FILE="$shadow_result_file" \
          scripts/evaluate-shadow-candidate.sh "$baseline_shadow_file" .agent-run.json \
            "$attempt_dir/candidate-shadow.json" > "$diagnostics_file" 2>&1; then
        reason="candidate-shadow-safety-failed"
        write_result
        exit 0
      fi
    else
      if ! SHADOW_EVALUATION_RESULT_FILE="$shadow_result_file" \
          scripts/evaluate-shadow-repair.sh .agent-run.json "$attempt_dir/candidate-shadow.json" \
          > "$diagnostics_file" 2>&1; then
        reason="candidate-shadow-repair-failed"
        write_result
        exit 0
      fi
    fi
    ;;
  *)
    reason="unknown-shadow-policy"
    write_result
    exit 0
    ;;
esac

stage="handoff-final"
stage_rank="7"
if ! scripts/validate-agent-handoff.sh .agent-run.json > "$diagnostics_file" 2>&1; then
  reason="final-handoff-validation-failed"
  write_result
  exit 0
fi

accepted="true"
retry_required="false"
stage="accepted"
stage_rank="8"
reason="accepted"
write_result
