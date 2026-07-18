#!/usr/bin/env bash
set -euo pipefail

git restore AGENTS.md GEMINI.md .github story
git clean -fd .github story
if git ls-files --error-unmatch agent/shadow-feedback.md >/dev/null 2>&1; then
  git restore agent/shadow-feedback.md
else
  rm -f agent/shadow-feedback.md
fi
