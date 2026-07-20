#!/usr/bin/env bash
set -euo pipefail

if (( $# < 2 )); then
  echo "Usage: $0 LEDGER_FILE ATTEMPT_RESULT [...]" >&2
  exit 2
fi

ledger_file="$1"
shift
available_results=()
for result_file in "$@"; do
  if [[ -f "$result_file" ]]; then
    available_results+=("$result_file")
  fi
done
if (( ${#available_results[@]} == 0 )); then
  echo "No completed agent-attempt results were supplied." >&2
  exit 1
fi

mkdir -p "$(dirname "$ledger_file")"
jq -s '.' "${available_results[@]}" > "$ledger_file"
latest_result="${available_results[$(( ${#available_results[@]} - 1 ))]}"
attempts_completed="${#available_results[@]}"
accepted="$(jq -r '.accepted' "$latest_result")"
acceptance="$(jq -r '.acceptance // "none"' "$latest_result")"
substantive_change="$(jq -r '.substantiveChange' "$latest_result")"
stage="$(jq -r '.stage' "$latest_result")"
reason="$(jq -r '.reason' "$latest_result")"
effect_classification="$(jq -r '.effectClassification // "unmeasured"' "$latest_result")"
exhausted="false"
if [[ "$accepted" != "true" && "$attempts_completed" -ge 3 ]]; then
  exhausted="true"
fi
best_candidate_commit="$(jq -r '
  [.[] | select((.candidateCommit // "") != "")] |
  sort_by(.stageRank, .attempt) |
  (last.candidateCommit // "")
' "$ledger_file")"
best_candidate_attempt="$(jq -r '
  [.[] | select((.candidateCommit // "") != "")] |
  sort_by(.stageRank, .attempt) |
  (last.attempt // 0)
' "$ledger_file")"

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "accepted=${accepted}"
    echo "acceptance=${acceptance}"
    echo "exhausted=${exhausted}"
    echo "attempts_completed=${attempts_completed}"
    echo "substantive_change=${substantive_change}"
    echo "stage=${stage}"
    echo "reason=${reason}"
    echo "effect_classification=${effect_classification}"
    echo "ledger_file=${ledger_file}"
    echo "best_candidate_commit=${best_candidate_commit}"
    echo "best_candidate_attempt=${best_candidate_attempt}"
  } >> "$GITHUB_OUTPUT"
fi

echo "Agent attempts resolved: completed=${attempts_completed}, accepted=${accepted}, acceptance=${acceptance}, exhausted=${exhausted}, stage=${stage}, reason=${reason}, effect=${effect_classification}, bestCandidateAttempt=${best_candidate_attempt}."
