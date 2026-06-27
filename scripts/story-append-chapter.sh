#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat >&2 <<'USAGE'
Usage:
  scripts/story-append-chapter.sh \
    --handoff-file PATH \
    --chapter-number N \
    --volume-number N \
    --volume-file PATH \
    --new-volume true|false \
    --current-state-commit COMMIT
USAGE
  exit 2
}

handoff_file=""
chapter_number=""
volume_number=""
volume_file=""
new_volume=""
current_state_commit=""

while (( $# > 0 )); do
  case "$1" in
    --handoff-file)
      handoff_file="${2:-}"
      shift 2
      ;;
    --chapter-number)
      chapter_number="${2:-}"
      shift 2
      ;;
    --volume-number)
      volume_number="${2:-}"
      shift 2
      ;;
    --volume-file)
      volume_file="${2:-}"
      shift 2
      ;;
    --new-volume)
      new_volume="${2:-}"
      shift 2
      ;;
    --current-state-commit)
      current_state_commit="${2:-}"
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

if [[ -z "$handoff_file" || -z "$chapter_number" || -z "$volume_number" || -z "$volume_file" || -z "$new_volume" || -z "$current_state_commit" ]]; then
  usage
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to parse ${handoff_file}." >&2
  exit 1
fi

if [[ ! -f "$handoff_file" ]]; then
  echo "Missing story handoff file: ${handoff_file}" >&2
  exit 1
fi

if ! jq -e '
  type == "object" and
  ((.chapterTitle // "") | type == "string" and length > 0) and
  ((.chapterBody // "") | type == "string" and length > 0) and
  ((.volumeTitle // "") | type == "string")
' "$handoff_file" >/dev/null; then
  echo "Story handoff JSON is malformed: ${handoff_file}" >&2
  exit 1
fi

chapter_title="$(jq -r '.chapterTitle' "$handoff_file" | tr '\n' ' ' | sed 's/[[:space:]]\+/ /g; s/^ //; s/ $//')"
chapter_body="$(jq -r '.chapterBody' "$handoff_file" | sed -e :a -e '/^\n*$/{$d;N;ba' -e '}')"
volume_title="$(jq -r '.volumeTitle // ""' "$handoff_file" | tr '\n' ' ' | sed 's/[[:space:]]\+/ /g; s/^ //; s/ $//')"
chapter_title="$(sed -E 's/^#+[[:space:]]*Chapter[[:space:]]+[0-9]+[[:space:]]+[—-][[:space:]]*//' <<<"$chapter_title")"
volume_title="$(sed -E 's/^#+[[:space:]]*Volume[[:space:]]+[0-9]+[[:space:]]+[—-][[:space:]]*//' <<<"$volume_title")"

if grep -q '^## Chapter '"$chapter_number"'[[:space:]—-]' "$volume_file" 2>/dev/null; then
  echo "Chapter ${chapter_number} already exists in ${volume_file}." >&2
  exit 1
fi

mkdir -p "$(dirname "$volume_file")"

if [[ "$new_volume" == "true" ]]; then
  if [[ -z "$volume_title" ]]; then
    echo "volumeTitle is required when creating a new volume." >&2
    exit 1
  fi

  if [[ -f "$volume_file" ]]; then
    echo "Refusing to create new volume because ${volume_file} already exists." >&2
    exit 1
  fi

  {
    printf '# Volume %s — %s\n' "$volume_number" "$volume_title"
    printf '\n'
  } > "$volume_file"
elif [[ ! -f "$volume_file" ]]; then
  echo "Expected existing volume file is missing: ${volume_file}" >&2
  exit 1
fi

{
  printf '\n'
  printf '## Chapter %s — %s\n' "$chapter_number" "$chapter_title"
  printf '\n'
  printf '%s\n' "$chapter_body"
} >> "$volume_file"

{
  cat <<'EOF'
# Chronicles of the AI Garden

The narrative history of the AI Live Garden: simulation changes state, agents change rules, and the chronicler turns committed state changes into story.

## Volumes

EOF

  find story/volumes -maxdepth 1 -type f -name 'volume-*.md' | sort | while IFS= read -r path; do
    title="$(sed -n 's/^# //p' "$path" | head -1)"
    count="$(awk '/^## Chapter / { count++ } END { print count + 0 }' "$path")"
    if (( count == 0 )); then
      continue
    fi
    number="$(sed -E 's/.*volume-0*([0-9]+)\.md/\1/' <<<"$path")"
    first=$(( ((number - 1) * 100) + 1 ))
    last=$(( first + count - 1 ))
    if (( first == last )); then
      chapter_label="Chapter ${first}"
    else
      chapter_label="Chapters ${first}-${last}"
    fi
    printf -- '- [%s](volumes/%s) — %s.\n' "$title" "$(basename "$path")" "$chapter_label"
  done
} > story/chronicles-of-ai-garden.md

echo "$current_state_commit" > story/last-narrated-garden-state.txt
rm -f story/context.md "$handoff_file"

echo "Appended story chapter ${chapter_number} to ${volume_file}."
