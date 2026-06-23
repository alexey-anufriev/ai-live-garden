#!/usr/bin/env bash
set -euo pipefail

if [[ "${GITHUB_ACTIONS:-}" != "true" ]]; then
  echo "restore-worktree-to-head.sh is only intended to run in GitHub Actions." >&2
  exit 2
fi

git reset --hard HEAD
git clean -fd
