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

has_substantive_agent_change() {
  scripts/agent-substantive-changes.sh >/dev/null
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

if git status --porcelain -uall | grep -Eq '^\?\? [^/]+\.(java|class|tmp|log|txt|py|sh|patch|diff)$'; then
  while IFS= read -r path; do
    add_hard_violation "Root scratch file must not be left in the worktree: ${path}"
  done < <(
    git status --porcelain -uall |
      sed -nE 's/^\?\? ([^/]+\.(java|class|tmp|log|txt|py|sh|patch|diff))$/\1/p'
  )
fi

repair_required="false"
if [[ "${AGENT_BASELINE_TEST_OUTCOME:-success}" != "success" || "${AGENT_BASELINE_POLICY_OUTCOME:-success}" != "success" ]]; then
  repair_required="true"
fi

if [[ "$scope" == "changed" && "$repair_required" != "true" ]] && ! has_substantive_agent_change; then
  add_hard_violation "Autonomous run must leave a substantive change under src/main/, src/test/, pom.xml, or data/garden-state.txt; a handoff or generated-memory-only diff is not a completed run."
fi

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
