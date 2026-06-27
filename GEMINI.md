# Gemini Instructions

Follow `AGENTS.md`.

Normal autonomous runs receive a compact context from `scripts/build-agent-context.sh`. Treat it as the first-pass view, then inspect raw files only when exact source, state, or memory details are needed.

This is a Java 25 / Maven project. Do not edit README state, summaries, journal, or current memory during autonomous runs; CI post-processing generates them from `.agent-run.json`.

Do not modify `AGENTS.md`, `GEMINI.md`, or `.github/` unless the human explicitly asks.
