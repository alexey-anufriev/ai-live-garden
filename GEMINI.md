# Gemini Instructions

Follow `AGENTS.md`.

Normal autonomous runs receive a compact context from `scripts/build-agent-context.sh`. Treat it as the first-pass view, then inspect raw files only when exact source, state, or memory details are needed.

This is a Java 25 / Maven project. During autonomous runs, do not edit generated memory files: `README.md`, `agent/state.md`, `agent/requests.md`, `agent/code-map.md`, `agent/garden-trends.svg`, `agent/organism-trends.svg`, `agent/journal/`, `agent/summaries/`, or `agent/templates/`. CI post-processing generates them from `.agent-run.json`. A valid run must leave both the handoff and a substantive implementation change; generated memory or a handoff alone does not count.

The Agent workflow may make one initial call and at most two bounded repair calls. A repair receives the current source diff and exact deterministic failure. Keep the same PM direction and locked evaluation criteria. A zero delta means the mechanism is causally inert in the bounded window: abandon it and change the production path that directly controls the metric; do not merely tune constants, thresholds, wording, or isolated tests. The third call is a final implementation redesign. CI independently runs tests and shadow validation, accepts a fully passing candidate immediately, may merge safe nonzero correctly directed progress after the final attempt, and preserves only zero-effect, wrong-direction, unsafe, or otherwise invalid candidates after exhaustion.

Do not modify `AGENTS.md`, `GEMINI.md`, or `.github/` unless the human explicitly asks.
