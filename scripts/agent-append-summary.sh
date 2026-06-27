#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat >&2 <<'USAGE'
Usage:
  scripts/agent-append-summary.sh --cadence daily|weekly|monthly|yearly --title TITLE --body BODY [--timestamp ISO_UTC] [--period PERIOD]

Daily summaries default to the current UTC date. Weekly, monthly, and yearly
summaries require --period matching the target file name without .md.
USAGE
  exit 2
}

cadence=""
title=""
body=""
timestamp="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
period=""

while (( $# > 0 )); do
  case "$1" in
    --cadence)
      cadence="${2:-}"
      shift 2
      ;;
    --title)
      title="${2:-}"
      shift 2
      ;;
    --body)
      body="${2:-}"
      shift 2
      ;;
    --timestamp)
      timestamp="${2:-}"
      shift 2
      ;;
    --period)
      period="${2:-}"
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

if [[ -z "$cadence" || -z "$title" || -z "$body" ]]; then
  usage
fi

if ! grep -Eq '^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z$' <<<"$timestamp"; then
  echo "Timestamp must be ISO-8601 UTC like YYYY-MM-DDTHH:MM:SSZ." >&2
  exit 2
fi

case "$cadence" in
  daily)
    period="${period:-${timestamp:0:10}}"
    dir="agent/summaries/daily"
    heading="# Daily Summary: ${period}"
    ;;
  weekly)
    [[ -n "$period" ]] || usage
    if ! grep -Eq '^[0-9]{4}-W[0-9]{2}$' <<<"$period"; then
      echo "Weekly period must look like YYYY-Www." >&2
      exit 2
    fi
    dir="agent/summaries/weekly"
    heading="# Weekly Summary: ${period}"
    ;;
  monthly)
    [[ -n "$period" ]] || usage
    if ! grep -Eq '^[0-9]{4}-[0-9]{2}$' <<<"$period"; then
      echo "Monthly period must look like YYYY-MM." >&2
      exit 2
    fi
    dir="agent/summaries/monthly"
    heading="# Monthly Summary: ${period}"
    ;;
  yearly)
    [[ -n "$period" ]] || usage
    if ! grep -Eq '^[0-9]{4}$' <<<"$period"; then
      echo "Yearly period must look like YYYY." >&2
      exit 2
    fi
    dir="agent/summaries/yearly"
    heading="# Yearly Summary: ${period}"
    ;;
  *)
    echo "Invalid cadence: $cadence" >&2
    usage
    ;;
esac

mkdir -p "$dir"
path="${dir}/${period}.md"

if [[ ! -e "$path" ]]; then
  {
    echo "$heading"
    echo
    echo "## Entries"
  } > "$path"
elif [[ "$cadence" != "daily" ]] && grep -q '^### ' "$path"; then
  echo "${cadence^} summary ${period} already has an entry; scheduled rollups are append-once." >&2
  exit 1
fi

{
  echo
  echo "### ${timestamp} - ${title}"
  echo
  printf '%s\n' "$body"
} >> "$path"

echo "$path"
