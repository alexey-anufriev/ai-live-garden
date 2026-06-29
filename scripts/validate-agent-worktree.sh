#!/usr/bin/env bash
set -euo pipefail

violations_file="${VALIDATE_AGENT_WORKTREE_VIOLATIONS_FILE:-}"
violations=()

add_violation() {
  violations+=("$1")
}

status_paths() {
  git status --porcelain -uall |
    sed '/^$/d' |
    sed 's/^...//; s/.* -> //' |
    sort -u
}

changed_test_paths() {
  status_paths |
    awk '/^src\/test\/java\/.+[.]java$/ { print }' |
    while IFS= read -r path; do
      [[ -f "$path" ]] && echo "$path"
    done
}

while IFS= read -r path; do
  [[ -n "$path" ]] || continue
  case "$path" in
    target/*)
      ;;
    *.class|*.jar|*.war|*.ear|*.zip|*.tar|*.tar.gz|*.tgz|*.log|*.tmp)
      add_violation "Generated or binary artifact must not be committed: ${path}"
      ;;
  esac
done < <(status_paths)

while IFS= read -r path; do
  [[ -n "$path" ]] || continue
  case "$path" in
    target/*)
      ;;
    *)
      add_violation "Tracked Java bytecode must be removed from the repository: ${path}"
      ;;
  esac
done < <(git ls-files '*.class')

if git status --porcelain -uall | grep -Eq '^\?\? [^/]+\.(java|class|tmp|log|txt)$'; then
  while IFS= read -r path; do
    add_violation "Root scratch file must not be left in the worktree: ${path}"
  done < <(
    git status --porcelain -uall |
      sed -nE 's/^\?\? ([^/]+\.(java|class|tmp|log|txt))$/\1/p'
  )
fi

if changed_test_paths | grep -q '/Test[^/]*[.]java$'; then
  while IFS= read -r path; do
    add_violation "Test file name is too generic; use a behavior-specific name: ${path}"
  done < <(changed_test_paths | grep '/Test[^/]*[.]java$' | sort)
fi

while IFS= read -r path; do
  [[ -n "$path" ]] || continue

  if grep -Eq 'traits\(\)\.contains' "$path" && ! grep -Eq 'nextCycle|Garden[[:space:]]|organisms\(\)|environment\(\)|events\(\)' "$path"; then
    add_violation "Test appears to assert trait storage without proving garden behavior: ${path}"
  fi

  if grep -Einq '\b(maybe|not sure|wait|does not distinguish|increased cycles|we need)\b' "$path"; then
    add_violation "Test contains scratch reasoning or uncertainty comments: ${path}"
  fi
done < <(changed_test_paths | sort)

if (( ${#violations[@]} > 0 )); then
  {
    echo "Agent worktree validation failed:"
    printf -- '- %s\n' "${violations[@]}"
  } >&2

  if [[ -n "$violations_file" ]]; then
    {
      echo "Agent worktree validation failed:"
      printf -- '- %s\n' "${violations[@]}"
    } > "$violations_file"
  fi
  exit 1
fi

if [[ -n "$violations_file" ]]; then
  : > "$violations_file"
fi
