# Autonomous run

Perform one autonomous evolution step for the AI Live Garden.

Process:

1. Read `AGENTS.md`, `GEMINI.md`, `README.md`, `agent/state.md`, `agent/requests.md`, recent active journal entries directly under `agent/journal/`, and the latest active summaries under `agent/summaries/`, excluding all `archive/` folders.
2. Inspect the current Java/Maven codebase and the persistent garden snapshot in `data/garden-state.txt`.
3. Choose exactly one small coherent next task. "Small" means conceptually focused, not necessarily limited to editing existing files. When choosing a task, consider whether the change can increase the garden's ecological diversity, interdependence, observability, or long-term behavioral depth without adding disconnected complexity.
   - A suitable task may create one or a few focused new files when that is the clearest way to express the change.
   - Do not avoid new files solely to keep the diff tiny. New source files, test files, renderers, reports, fixtures, summaries, or documentation pages are welcome when they fit the chosen step.
4. If the chosen task requires a new dependency, external tool, CI change, protected file change, network integration, or expanded permission, do not perform it directly. Record a request in `agent/requests.md` and choose a smaller task that fits the current constraints.
5. Make the change.
6. Run `mvn test` if possible.
7. Do not manually fabricate large arbitrary changes to `data/garden-state.txt`; normal garden evolution happens when the simulation loads the persistent state, advances it, and saves it again. The workflow also advances the persistent garden state after your code change.
8. Update `agent/state.md`.
9. Update the `Current Garden State` section in `README.md` between the protected markers only. The first line inside the protected block must be the Garden Health line: `**Garden Health:** SYMBOL Status — one short reason.`. The second line - short description of the current garden state.
10. Add one new journal entry under `agent/journal/` by copying `agent/templates/journal-entry.md` and replacing only the `{{PLACEHOLDER}}` text. Keep the copied headings exactly as written.
11. Update the current daily summary by appending one timestamped entry to `agent/summaries/daily/YYYY-MM-DD.md`. Do not delete, replace, shorten, reorder, or rewrite existing summary content.
12. Update rollup summaries only when due: weekly only on Monday's first run for the previous ISO week from daily summaries; monthly only on the 1st day of the month for the previous calendar month from weekly summaries; yearly only on January 1 for the previous calendar year from monthly summaries. If a rollup is not due or was already added for that period, do not create or update it.
13. Do not modify `AGENTS.md`, `GEMINI.md`, or `.github/` unless the human explicitly asks for an experiment-rule change.
14. Do not modify or delete previous journal entries.
15. Do not ask the human what to do next.

The journal entry must use the template in `agent/templates/journal-entry.md`.

Prefer continuity over novelty.

Make the garden slightly more expressive, coherent, observable, or maintainable than before.
