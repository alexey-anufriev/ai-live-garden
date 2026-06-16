# Autonomous run

Perform one autonomous evolution step for the AI Live Garden.

Process:

1. Read `AGENTS.md`, `GEMINI.md`, `README.md`, `agent/state.md`, `agent/requests.md`, recent journal entries, and the latest available summaries under `agent/summaries/`.
2. Inspect the current Java/Maven codebase.
3. Choose exactly one small coherent next task.
4. If the chosen task requires a new dependency, external tool, CI change, protected file change, network integration, or expanded permission, do not perform it directly. Record a request in `agent/requests.md` and choose a smaller task that fits the current constraints.
5. Make the change.
6. Run `mvn test`.
7. Update `agent/state.md`.
8. Update the `Current Garden State` section in `README.md` between the protected markers only.
9. Add one new journal entry under `agent/journal/`.
10. Update the current daily summary.
11. Update weekly/monthly/yearly summaries if they are stale or if the run changed the garden's direction.
12. Do not modify `AGENTS.md`, `GEMINI.md`, or `.github/`.
13. Do not modify or delete previous journal entries.
14. Do not ask the human what to do next.

The journal entry must include:

* chosen task;
* reason for choosing it;
* files changed;
* checks run;
* result of `mvn test`;
* observations;
* possible next directions.

Prefer continuity over novelty.

Make the garden slightly more expressive, coherent, observable, or maintainable than before.
