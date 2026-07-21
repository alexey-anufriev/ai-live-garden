#!/usr/bin/env bash
set -euo pipefail

if (( $# != 5 )); then
  echo "Usage: $0 CANDIDATE_COMMIT RUN_MODE LEDGER_FILE HANDOFF_FILE VERDICT_FILE" >&2
  exit 2
fi

candidate_commit="$1"
run_mode="$2"
ledger_file="$3"
handoff_file="$4"
verdict_file="$5"

if [[ ! "$candidate_commit" =~ ^[0-9a-f]{40}$ ]] || ! git cat-file -e "${candidate_commit}^{commit}" 2>/dev/null; then
  echo "Accepted candidate commit is missing or invalid: ${candidate_commit}" >&2
  exit 1
fi
if [[ ! -f "$handoff_file" ]]; then
  echo "Saved accepted handoff is missing: ${handoff_file}" >&2
  exit 1
fi

git restore --worktree --staged -- .
# This runs only in the disposable CI checkout after the substantive candidate
# was snapshotted. Remove every untracked residue before restoring that exact
# candidate so a late worktree-policy failure cannot leak into the fallback.
git clean -fd -- . >/dev/null

candidate_patch="$(mktemp "${RUNNER_TEMP:-/tmp}/accepted-candidate.XXXXXX")"
trap 'rm -f "$candidate_patch"' EXIT
git diff --binary HEAD "$candidate_commit" -- src/main src/test pom.xml data/garden-state.txt > "$candidate_patch"
if [[ ! -s "$candidate_patch" ]]; then
  echo "Accepted candidate commit contains no substantive patch." >&2
  exit 1
fi
git apply "$candidate_patch"

if [[ "$run_mode" == "evolution" ]]; then
  if [[ ! -f "$verdict_file" ]]; then
    scripts/record-agent-verdict.sh "$ledger_file" "$handoff_file" "$verdict_file"
  fi
  mkdir -p agent
  cp "$verdict_file" agent/shadow-feedback.md
  if [[ -n "${AGENT_FINALIZATION_REASON:-}" ]]; then
    {
      echo
      echo "## Harness Finalization"
      echo
      echo "The accepted source and measured verdict were preserved, but the garden tick and generated-memory transaction were rolled back because: ${AGENT_FINALIZATION_REASON}."
    } >> agent/shadow-feedback.md
  fi
fi

rm -f .agent-run.json
if ! scripts/agent-substantive-changes.sh >/dev/null; then
  echo "Fallback did not restore the accepted substantive change." >&2
  exit 1
fi

trap - EXIT
rm -f "$candidate_patch"
echo "Prepared accepted candidate fallback without the failed tick or generated-memory transaction."
