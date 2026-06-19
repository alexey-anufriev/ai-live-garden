#!/usr/bin/env bash
set -euo pipefail

git restore AGENTS.md GEMINI.md .github story
git clean -fd .github story
