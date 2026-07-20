# Gemini Instructions

Follow `AGENTS.md`.

Normal autonomous runs receive a compact context from `scripts/build-agent-context.sh`. Treat it as the first-pass view, then inspect raw files only when exact source, state, or memory details are needed.

This is a Java 25 / Maven project. During autonomous runs, do not edit generated memory files: `README.md`, `agent/state.md`, `agent/requests.md`, `agent/code-map.md`, `agent/garden-trends.svg`, `agent/organism-trends.svg`, `agent/journal/`, `agent/summaries/`, or `agent/templates/`. CI post-processing generates them from `.agent-run.json`. A valid run must leave both the handoff and a substantive implementation change; generated memory or a handoff alone does not count.

The Agent workflow makes exactly one agent call per run. CI independently runs tests, policy checks, and same-state shadow evaluation. Safe coherent code is committed even when its ecological target is missed; CI records a `target-met`, `partial-progress`, `inert`, or `wrong-direction` verdict as mandatory input for the next run. When previous verdict feedback exists, inspect the implementation already on main and explicitly decide to keep, revise, or revert it. A zero delta means the mechanism was causally inert in the bounded window; wrong-direction or inert code should normally be corrected or reverted before adding a disconnected mechanism. Invalid, test-failing, unmeasured, policy-violating, or unsafe candidates are preserved off main with exact evidence.

Do not modify `AGENTS.md`, `GEMINI.md`, or `.github/` unless the human explicitly asks.
