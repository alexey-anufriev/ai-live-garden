# Gemini Instructions

Follow `AGENTS.md`.

Normal autonomous runs receive a compact context from `scripts/build-agent-context.sh`. Treat it as the first-pass view, then inspect raw files only when exact source, state, or memory details are needed.

This is a Java 25 / Maven project. During autonomous runs, do not edit generated memory files: `README.md`, `agent/state.md`, `agent/requests.md`, `agent/code-map.md`, `agent/garden-trends.svg`, `agent/organism-trends.svg`, `agent/journal/`, `agent/summaries/`, or `agent/templates/`. CI post-processing generates them from `.agent-run.json`. A valid run must leave both the handoff and a substantive implementation change; generated memory or a handoff alone does not count.

Do not modify `AGENTS.md`, `GEMINI.md`, or `.github/` unless the human explicitly asks.
