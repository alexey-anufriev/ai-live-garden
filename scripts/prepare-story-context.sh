#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${GITHUB_OUTPUT:-}" ]]; then
  echo "GITHUB_OUTPUT is not set." >&2
  exit 2
fi

if [[ -z "${RUNNER_TEMP:-}" ]]; then
  RUNNER_TEMP="$(mktemp -d)"
fi

current_state_commit="$(git log -1 --format=%H -- data/garden-state.txt)"

if [[ -f story/last-narrated-garden-state.txt ]]; then
  last_narrated_commit="$(tr -d '[:space:]' < story/last-narrated-garden-state.txt)"
else
  last_narrated_commit=""
fi

echo "Current garden-state commit: $current_state_commit"
echo "Last narrated garden-state commit: ${last_narrated_commit:-none}"

if [[ -n "$last_narrated_commit" && "$last_narrated_commit" == "$current_state_commit" ]]; then
  echo "No new garden-state commit to narrate."
  echo "skip=true" >> "$GITHUB_OUTPUT"
  exit 0
fi

echo "skip=false" >> "$GITHUB_OUTPUT"
echo "current_state_commit=$current_state_commit" >> "$GITHUB_OUTPUT"

volume_files=(story/volumes/volume-*.md)

if [[ ! -e "${volume_files[0]}" ]]; then
  chapter_count=0
else
  chapter_count="$(
    awk '/^## Chapter / { count++ } END { print count + 0 }' "${volume_files[@]}"
  )"
fi

next_chapter=$((chapter_count + 1))
volume_number=$(( ((next_chapter - 1) / 100) + 1 ))
volume_file="$(printf "story/volumes/volume-%04d.md" "$volume_number")"

new_volume="false"
if [[ ! -f "$volume_file" ]]; then
  new_volume="true"
fi

echo "next_chapter=$next_chapter" >> "$GITHUB_OUTPUT"
echo "volume_number=$volume_number" >> "$GITHUB_OUTPUT"
echo "volume_file=$volume_file" >> "$GITHUB_OUTPUT"
echo "new_volume=$new_volume" >> "$GITHUB_OUTPUT"

emit_bounded_file() {
  local file="$1"
  local max_lines="$2"
  local line_count

  line_count="$(wc -l < "$file" | tr -d '[:space:]')"
  if (( line_count <= max_lines )); then
    cat "$file"
    return
  fi

  local edge_lines=$((max_lines / 2))
  sed -n "1,${edge_lines}p" "$file"
  echo
  echo "[... omitted $((line_count - (edge_lines * 2))) lines ...]"
  echo
  tail -n "$edge_lines" "$file"
}

{
  echo "# Story Context"
  echo
  echo "Requested chapter number: $next_chapter"
  echo "Requested volume number: $volume_number"
  echo "Requested volume file: \`$volume_file\`"
  echo "Creates new volume: $new_volume"
  echo

  if [[ "$new_volume" == "true" ]]; then
    echo "This chapter starts a new volume."
    echo "Generate a volumeTitle for the JSON handoff."
  else
    echo "This chapter continues an existing volume."
    echo "Set volumeTitle to an empty string in the JSON handoff."
  fi

  echo
  echo "The chapter title and body must be generated from the actual garden events."
  echo "Do not edit story files directly; CI will append the chapter from \`.story-run.json\`."
  echo

  if [[ -n "$last_narrated_commit" ]]; then
    echo "## Previous narrated garden context"
    echo
    echo '```text'
    git show "$last_narrated_commit:data/garden-state.txt" > "$RUNNER_TEMP/previous-garden-state.txt"
    emit_bounded_file "$RUNNER_TEMP/previous-garden-state.txt" 120
    echo '```'
    echo
    echo "## Bounded garden-state diff since last narrated commit"
    echo
    echo '```diff'
    git diff --unified=0 --output="$RUNNER_TEMP/garden-state.diff" "$last_narrated_commit" "$current_state_commit" -- data/garden-state.txt
    sed -n '1,220p' "$RUNNER_TEMP/garden-state.diff"
    echo '```'
    echo
  else
    echo "## Previous narrated garden context"
    echo
    echo "No previous narrated snapshot exists. Write the opening chapter of the chronicle."
    echo
  fi

  echo "## Current garden context"
  echo
  echo '```text'
  emit_bounded_file data/garden-state.txt 160
  echo '```'
  echo

  echo "## Recent current state tail"
  echo
  echo '```text'
  tail -80 data/garden-state.txt
  echo '```'
  echo

  echo "## Chronicle index"
  echo
  echo '```md'
  cat story/chronicles-of-ai-garden.md
  echo '```'
  echo

  if [[ -f "$volume_file" ]]; then
    echo "## Current volume so far"
    echo
    echo '```md'
    tail -160 "$volume_file"
    echo '```'
  else
    echo "## Current volume so far"
    echo
    echo "This volume does not exist yet."
  fi
} > story/context.md

{
  echo 'text<<EOF'
  cat .github/gemini/daily-story.md
  echo
  echo
  cat story/context.md
  echo 'EOF'
} >> "$GITHUB_OUTPUT"
