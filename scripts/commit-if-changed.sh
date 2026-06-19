#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${COMMIT_MESSAGE_PREFIX:-}" ]]; then
  echo "COMMIT_MESSAGE_PREFIX is required." >&2
  exit 2
fi

if [[ -z "${GITHUB_REF_NAME:-}" ]]; then
  echo "GITHUB_REF_NAME is required." >&2
  exit 2
fi

timestamp_format="${COMMIT_TIMESTAMP_FORMAT:-%Y-%m-%dT%H:%MZ}"
message="${COMMIT_MESSAGE_PREFIX} $(date -u +"${timestamp_format}")"

if (( $# == 0 )); then
  pathspecs=(".")
else
  pathspecs=("$@")
fi

if [[ -z "$(git status --porcelain -- "${pathspecs[@]}")" ]]; then
  echo "No changes to commit."
  exit 0
fi

git config user.name "ai-live-garden[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add -- "${pathspecs[@]}"

if git diff --cached --quiet; then
  echo "No staged changes to commit."
  exit 0
fi

git commit -m "$message"
git pull --rebase origin "$GITHUB_REF_NAME"
git push origin "HEAD:${GITHUB_REF_NAME}"
