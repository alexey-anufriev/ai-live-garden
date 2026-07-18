#!/usr/bin/env bash
set -euo pipefail

substantive_paths="$(
  git status --porcelain -uall |
    sed '/^$/d' |
    sed 's/^...//; s/.* -> //' |
    awk '/^src\/main\// || /^src\/test\// || $0 == "pom.xml" || $0 == "data/garden-state.txt"' |
    sort -u
)"

if [[ -z "$substantive_paths" ]]; then
  exit 1
fi

printf '%s\n' "$substantive_paths"
