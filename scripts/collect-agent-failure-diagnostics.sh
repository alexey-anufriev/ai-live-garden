#!/usr/bin/env bash
set -euo pipefail

output_dir="${1:-}"
if [[ -z "$output_dir" ]]; then
  echo "Usage: $0 OUTPUT_DIR" >&2
  exit 2
fi

mkdir -p "$output_dir"

git status --short > "$output_dir/git-status-short.txt"
git diff --stat > "$output_dir/git-diff-stat.txt"
git diff --binary > "$output_dir/git-diff.patch"
git diff --name-status > "$output_dir/git-diff-name-status.txt"
git ls-files --others --exclude-standard > "$output_dir/git-untracked-files.txt"

if [[ -f .agent-run.json ]]; then
  cp .agent-run.json "$output_dir/agent-run.json"
fi

if [[ -d gemini-artifacts ]]; then
  mkdir -p "$output_dir/gemini-artifacts"
  cp -R gemini-artifacts/. "$output_dir/gemini-artifacts/"
fi

if [[ -n "${AGENT_PRIMARY_GEMINI_ARTIFACTS_DIR:-}" && -d "${AGENT_PRIMARY_GEMINI_ARTIFACTS_DIR}" ]]; then
  mkdir -p "$output_dir/gemini-artifacts-primary"
  cp -R "${AGENT_PRIMARY_GEMINI_ARTIFACTS_DIR}/." "$output_dir/gemini-artifacts-primary/"
fi

if [[ -s "$output_dir/git-untracked-files.txt" ]]; then
  mkdir -p "$output_dir/untracked-files"
  while IFS= read -r path; do
    if [[ -f "$path" ]]; then
      mkdir -p "$output_dir/untracked-files/$(dirname "$path")"
      cp "$path" "$output_dir/untracked-files/$path"
    fi
  done < "$output_dir/git-untracked-files.txt"
fi

if [[ -d target/surefire-reports ]]; then
  mkdir -p "$output_dir/surefire-reports"
  cp -R target/surefire-reports/. "$output_dir/surefire-reports/"
fi

if [[ -n "${AGENT_CONTEXT_FILE:-}" && -f "$AGENT_CONTEXT_FILE" ]]; then
  cp "$AGENT_CONTEXT_FILE" "$output_dir/agent-context.md"
fi

if [[ -n "${AGENT_CONTEXT_METADATA_FILE:-}" && -f "$AGENT_CONTEXT_METADATA_FILE" ]]; then
  cp "$AGENT_CONTEXT_METADATA_FILE" "$output_dir/agent-context.metadata"
fi

if [[ -n "${AGENT_BASELINE_TEST_RESULT_FILE:-}" && -f "$AGENT_BASELINE_TEST_RESULT_FILE" ]]; then
  cp "$AGENT_BASELINE_TEST_RESULT_FILE" "$output_dir/baseline-test-result.md"
fi

if [[ -n "${AGENT_BASELINE_POLICY_RESULT_FILE:-}" && -f "$AGENT_BASELINE_POLICY_RESULT_FILE" ]]; then
  cp "$AGENT_BASELINE_POLICY_RESULT_FILE" "$output_dir/baseline-policy-result.md"
fi

for shadow_variable in AGENT_BASELINE_SHADOW_FILE AGENT_CANDIDATE_SHADOW_FILE AGENT_SHADOW_EVALUATION_RESULT_FILE \
  AGENT_SHADOW_RETRY_CANDIDATE_FILE AGENT_SHADOW_RETRY_EVALUATION_RESULT_FILE; do
  shadow_file="${!shadow_variable:-}"
  if [[ -n "$shadow_file" && -f "$shadow_file" ]]; then
    cp "$shadow_file" "$output_dir/$(basename "$shadow_file")"
  fi
done

if compgen -G "target/*.dump" > /dev/null || compgen -G "target/*.dumpstream" > /dev/null; then
  mkdir -p "$output_dir/maven-dumps"
  cp target/*.dump target/*.dumpstream "$output_dir/maven-dumps/" 2>/dev/null || true
fi

{
  echo "# Agent Failure Diagnostics"
  echo
  echo "Generated at: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo
  echo "## Changed files"
  echo
  if [[ -s "$output_dir/git-diff-name-status.txt" ]]; then
    sed 's/^/- /' "$output_dir/git-diff-name-status.txt"
  else
    echo "- No tracked file changes"
  fi
  echo
  echo "## Git status"
  echo
  if [[ -s "$output_dir/git-status-short.txt" ]]; then
    sed 's/^/- /' "$output_dir/git-status-short.txt"
  else
    echo "- Clean tracked worktree"
  fi
  echo
  echo "## Untracked files"
  echo
  if [[ -s "$output_dir/git-untracked-files.txt" ]]; then
    sed 's/^/- /' "$output_dir/git-untracked-files.txt"
  else
    echo "- None"
  fi
  echo
  echo "## Compact context"
  echo
  if [[ -f "$output_dir/agent-context.metadata" ]]; then
    sed 's/^/- /' "$output_dir/agent-context.metadata"
  else
    echo "- Context metadata not available"
  fi
  if [[ -f "$output_dir/agent-context.md" ]]; then
    echo "- Full compact context copied to \`agent-context.md\`"
  fi
  if [[ -f "$output_dir/agent-run.json" ]]; then
    echo "- Agent handoff copied to \`agent-run.json\`"
  fi
  if [[ -d "$output_dir/gemini-artifacts" ]]; then
    echo "- Gemini artifacts copied to \`gemini-artifacts/\`"
  fi
  if [[ -d "$output_dir/gemini-artifacts-primary" ]]; then
    echo "- Primary Gemini artifacts copied to \`gemini-artifacts-primary/\`"
  fi
  if [[ -f "$output_dir/baseline-test-result.md" ]]; then
    echo "- Baseline Maven test result copied to \`baseline-test-result.md\`"
  fi
  if [[ -f "$output_dir/baseline-policy-result.md" ]]; then
    echo "- Baseline worktree policy result copied to \`baseline-policy-result.md\`"
  fi
  if [[ -f "$output_dir/shadow-evaluation-result.json" ]]; then
    echo "- Shadow evaluation result copied to \`shadow-evaluation-result.json\`"
  fi
} > "$output_dir/README.md"
