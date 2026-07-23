#!/usr/bin/env bash
set -euo pipefail

timeout_seconds="${MAVEN_TEST_TIMEOUT_SECONDS:-180}"
if ! [[ "$timeout_seconds" =~ ^[1-9][0-9]*$ ]]; then
  echo "MAVEN_TEST_TIMEOUT_SECONDS must be a positive integer." >&2
  exit 2
fi

exec timeout --signal=TERM --kill-after=15s "${timeout_seconds}s" mvn -B test -Dtest.mode=true
