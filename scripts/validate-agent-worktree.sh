#!/usr/bin/env bash
set -euo pipefail

violations_file="${VALIDATE_AGENT_WORKTREE_VIOLATIONS_FILE:-}"
scope="${VALIDATE_AGENT_WORKTREE_SCOPE:-changed}"
hard_violations=()
deferred_violations=()

add_hard_violation() {
  hard_violations+=("$1")
}

add_deferred_violation() {
  deferred_violations+=("$1")
}

status_paths() {
  git status --porcelain -uall |
    sed '/^$/d' |
    sed 's/^...//; s/.* -> //' |
    sort -u
}

test_paths() {
  case "$scope" in
    changed)
      status_paths |
        awk '/^src\/test\/java\/.+[.]java$/ { print }' |
        while IFS= read -r path; do
          [[ -f "$path" ]] && echo "$path"
        done
      ;;
    all-tests|baseline)
      git ls-files 'src/test/java' |
        awk '/[.]java$/ { print }' |
        while IFS= read -r path; do
          [[ -f "$path" ]] && echo "$path"
        done
      ;;
    *)
      echo "Unknown VALIDATE_AGENT_WORKTREE_SCOPE: ${scope}" >&2
      exit 2
      ;;
  esac
}

while IFS= read -r path; do
  [[ -n "$path" ]] || continue
  case "$path" in
    target/*)
      ;;
    *.class|*.jar|*.war|*.ear|*.zip|*.tar|*.tar.gz|*.tgz|*.log|*.tmp)
      add_hard_violation "Generated or binary artifact must not be committed: ${path}"
      ;;
  esac
done < <(status_paths)

while IFS= read -r path; do
  [[ -n "$path" ]] || continue
  case "$path" in
    target/*)
      ;;
    *)
      add_hard_violation "Tracked Java bytecode must be removed from the repository: ${path}"
      ;;
  esac
done < <(git ls-files '*.class')

if git status --porcelain -uall | grep -Eq '^\?\? [^/]+\.(java|class|tmp|log|txt)$'; then
  while IFS= read -r path; do
    add_hard_violation "Root scratch file must not be left in the worktree: ${path}"
  done < <(
    git status --porcelain -uall |
      sed -nE 's/^\?\? ([^/]+\.(java|class|tmp|log|txt))$/\1/p'
  )
fi

if [[ "$scope" == "changed" ]] && test_paths | grep -q '/Test[^/]*[.]java$'; then
  while IFS= read -r path; do
    add_deferred_violation "Test file name is too generic; use a behavior-specific name: ${path}"
  done < <(test_paths | grep '/Test[^/]*[.]java$' | sort)
fi

while IFS= read -r path; do
  [[ -n "$path" ]] || continue

  if grep -Eq 'traits\(\)\.contains' "$path" && ! grep -Eq 'nextCycle|Garden[[:space:]]|organisms\(\)|environment\(\)|events\(\)' "$path"; then
    if [[ "$scope" == "changed" ]]; then
      add_deferred_violation "Test appears to assert trait storage without proving garden behavior: ${path}"
    fi
  fi

  if grep -Einq '^[[:space:]]*//.*\b(maybe|not sure|wait|does not distinguish|increased cycles|we need)\b' "$path"; then
    add_deferred_violation "Test contains scratch reasoning or uncertainty comments: ${path}"
  fi
done < <(test_paths | sort)

severity="clean"
exit_code=0
if (( ${#hard_violations[@]} > 0 )); then
  severity="hard"
  exit_code=1
elif (( ${#deferred_violations[@]} > 0 )); then
  severity="deferred"
  exit_code=10
fi

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "severity=${severity}"
    echo "hard_count=${#hard_violations[@]}"
    echo "deferred_count=${#deferred_violations[@]}"
  } >> "$GITHUB_OUTPUT"
fi

if [[ "$severity" != "clean" ]]; then
  {
    echo "Agent worktree validation failed:"
    if (( ${#hard_violations[@]} > 0 )); then
      echo "Hard violations:"
      printf -- '- %s\n' "${hard_violations[@]}"
    fi
    if (( ${#deferred_violations[@]} > 0 )); then
      echo "Deferred repair violations:"
      printf -- '- %s\n' "${deferred_violations[@]}"
    fi
  } >&2

  if [[ -n "$violations_file" ]]; then
    {
      echo "Agent worktree validation failed:"
      echo "- Severity: ${severity}"
      echo "- Scope: ${scope}"
      if (( ${#hard_violations[@]} > 0 )); then
        echo
        echo "Hard violations:"
        printf -- '- %s\n' "${hard_violations[@]}"
      fi
      if (( ${#deferred_violations[@]} > 0 )); then
        echo
        echo "Deferred repair violations:"
        printf -- '- %s\n' "${deferred_violations[@]}"
      fi
    } > "$violations_file"
  fi
  exit "$exit_code"
fi

if [[ -n "$violations_file" ]]; then
  {
    echo "Agent worktree validation passed."
    echo "- Severity: clean"
    echo "- Scope: ${scope}"
  } > "$violations_file"
fi
