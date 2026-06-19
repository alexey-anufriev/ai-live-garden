#!/usr/bin/env bash
set -euo pipefail

if (( $# != 1 )); then
  echo "Usage: $0 CURRENT_STATE_COMMIT" >&2
  exit 2
fi

echo "$1" > story/last-narrated-garden-state.txt
rm -f story/context.md
