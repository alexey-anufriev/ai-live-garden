#!/usr/bin/env bash
set -euo pipefail

archive_cadence() {
  local cadence="$1"
  local limit="$2"
  local pattern="$3"
  local folder="agent/summaries/${cadence}"
  local archive="${folder}/archive"

  mkdir -p "$archive"

  if [[ ! -d "$folder" ]]; then
    echo "Summary folder ${folder} does not exist; skipping."
    return
  fi

  mapfile -t summary_files < <(find "$folder" -maxdepth 1 -type f -name "$pattern" | sort)
  local active_count="${#summary_files[@]}"

  if (( active_count <= limit )); then
    echo "Active ${cadence} summaries: ${active_count}; no archive move needed."
    return
  fi

  local archive_count=$((active_count - limit))
  echo "Archiving ${archive_count} old ${cadence} summaries."

  for ((i = 0; i < archive_count; i++)); do
    mv "${summary_files[$i]}" "$archive/"
  done
}

archive_cadence "daily" 100 "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].md"
archive_cadence "weekly" 50 "[0-9][0-9][0-9][0-9]-W[0-9][0-9].md"
archive_cadence "monthly" 12 "[0-9][0-9][0-9][0-9]-[0-9][0-9].md"
archive_cadence "yearly" 10 "[0-9][0-9][0-9][0-9].md"
