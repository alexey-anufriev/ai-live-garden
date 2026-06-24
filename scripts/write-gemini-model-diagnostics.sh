#!/usr/bin/env bash
set -euo pipefail

value_or_dash() {
  local value="${1:-}"
  if [[ -n "$value" ]]; then
    printf '%s\n' "$value"
  else
    printf '%s\n' "-"
  fi
}

cli_path="$(command -v gemini || true)"
node_path="$(command -v node || true)"
npm_path="$(command -v npm || true)"

cli_version="-"
if [[ -n "$cli_path" ]]; then
  cli_version="$(gemini --version 2>&1 || true)"
  if [[ -z "$cli_version" ]]; then
    cli_version="-"
  fi
fi

package_version="-"
if [[ -n "$npm_path" ]]; then
  package_version="$(npm list -g @google/gemini-cli --depth=0 2>/dev/null | sed -n '2p' || true)"
  if [[ -z "$package_version" ]]; then
    package_version="-"
  fi
fi

{
  echo "## Gemini Model Diagnostics"
  echo
  echo "- Requested execution model: $(value_or_dash "${EXECUTION_MODEL:-}")"
  echo "- Requested context compaction model: $(value_or_dash "${CONTEXT_COMPACTION_MODEL:-}")"
  echo "- Resolved gemini binary: $(value_or_dash "$cli_path")"
  echo "- Gemini CLI version: ${cli_version}"
  echo "- Global Gemini CLI package: ${package_version}"
  echo "- Node: $(value_or_dash "$node_path")"
  echo "- npm: $(value_or_dash "$npm_path")"
  echo
  echo "This records the model requested by the workflow before Gemini runs. Provider-side model mapping or quota attribution must be read from Gemini CLI/API errors if a call fails."
} | tee "${RUNNER_TEMP:-/tmp}/gemini-model-diagnostics.md"

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  cat "${RUNNER_TEMP:-/tmp}/gemini-model-diagnostics.md" >> "$GITHUB_STEP_SUMMARY"
fi
