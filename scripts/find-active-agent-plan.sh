#!/usr/bin/env bash
set -euo pipefail

plans_dir="${AGENT_PM_PLANS_DIR:-agent/plans}"
max_age_days="${AGENT_PM_PLAN_MAX_AGE_DAYS:-0}"
reference_date="${AGENT_PM_REFERENCE_DATE:-$(date -u +%Y-%m-%d)}"

if ! [[ "$max_age_days" =~ ^[0-9]+$ ]]; then
  echo "AGENT_PM_PLAN_MAX_AGE_DAYS must be a non-negative integer." >&2
  exit 2
fi

if ! [[ "$reference_date" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]]; then
  echo "AGENT_PM_REFERENCE_DATE must use YYYY-MM-DD." >&2
  exit 2
fi

latest_plan="$(find "$plans_dir" -maxdepth 1 -type f -name '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].md' -print 2>/dev/null | sort -V | tail -n 1)"
if [[ -z "$latest_plan" ]]; then
  exit 0
fi

plan_date="$(basename "$latest_plan" .md)"
reference_epoch="$(date -u -d "$reference_date" +%s)"
plan_epoch="$(date -u -d "$plan_date" +%s)"
age_days=$(( (reference_epoch - plan_epoch) / 86400 ))

if (( age_days < 0 || age_days > max_age_days )); then
  exit 0
fi

echo "$latest_plan"
