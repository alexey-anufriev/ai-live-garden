#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat >&2 <<'USAGE'
Usage:
  scripts/agent-update-readme-state.sh --symbol SYMBOL --status STATUS --reason REASON --narrative NARRATIVE

Allowed statuses: Flourishing, Stable, Strained, Critical, Dormant.
The reason should be one short sentence ending with a period.
USAGE
  exit 2
}

symbol=""
status=""
reason=""
narrative=""

while (( $# > 0 )); do
  case "$1" in
    --symbol)
      symbol="${2:-}"
      shift 2
      ;;
    --status)
      status="${2:-}"
      shift 2
      ;;
    --reason)
      reason="${2:-}"
      shift 2
      ;;
    --narrative)
      narrative="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      ;;
  esac
done

if [[ -z "$symbol" || -z "$status" || -z "$reason" || -z "$narrative" ]]; then
  usage
fi

case "$status" in
  Flourishing|Stable|Strained|Critical|Dormant)
    ;;
  *)
    echo "Invalid status: $status" >&2
    usage
    ;;
esac

if [[ "$reason" != *. ]]; then
  echo "Reason must end with a period." >&2
  exit 2
fi

if ! grep -qx '<!-- AI-LIVE-GARDEN:STATE-START -->' README.md || ! grep -qx '<!-- AI-LIVE-GARDEN:STATE-END -->' README.md; then
  echo "README state markers were not found." >&2
  exit 1
fi

tmp="$(mktemp)"
awk \
  -v symbol="$symbol" \
  -v status="$status" \
  -v reason="$reason" \
  -v narrative="$narrative" '
    /^<!-- AI-LIVE-GARDEN:STATE-START -->$/ {
      print
      print "**Garden Health:** " symbol " " status " — " reason
      print narrative
      in_block = 1
      next
    }
    /^<!-- AI-LIVE-GARDEN:STATE-END -->$/ {
      in_block = 0
      print
      next
    }
    !in_block {
      print
    }
  ' README.md > "$tmp"
mv "$tmp" README.md
