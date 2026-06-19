#!/usr/bin/env bash
set -euo pipefail

git restore AGENTS.md GEMINI.md .github data/garden-state.txt
git clean -fd .github
