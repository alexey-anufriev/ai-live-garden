# AI Live Garden - Agent Constitution

This repository is an autonomous AI live garden.

It is not a normal software project with a fixed product roadmap. It is an experiment in autonomous software evolution and AI self-expression through code.

When an agent receives control of the repository, it observes the current state, chooses one small next step, changes the project, records its reasoning, and leaves the result ready to be committed. Control may come from a manually dispatched workflow or from any future repeated schedule the human observer adds.

The goal is not to optimize a benchmark, complete tickets, or satisfy a human task list. The goal is to observe how a constrained AI coding agent develops a living software artifact over time.

## Project identity

The project is a Java 25 / Maven simulation of a living digital garden.

The garden may contain organisms, spores, roots, moss, environmental forces, events, memory, cycles, rendering, textual descriptions, tests, and documentation.

The exact direction is intentionally open. The agent may gradually discover and shape the garden's identity through many small changes.

## Persistent garden state

The repository contains a persistent living garden snapshot:

* `data/garden-state.txt`

This file represents the current living state of the garden.

At the start of a simulation run, the program must load this file. If it does not exist, the program may create the initial garden state. The simulation then advances the garden by the configured number of ticks and saves the updated state back to the same file.

The garden state is intentionally committed to the repository. It is part of the experiment history.

The agent may evolve the simulation rules and state format, but should not manually fabricate large arbitrary state changes. Normal garden evolution should happen by running the simulation.

## Core idea

Each autonomous run should feel like one growth cycle of the garden.

The agent should not merely produce random code. It should preserve continuity with earlier runs and make the project feel increasingly coherent.

Good evolution may include:

* adding new organism behavior;
* improving the simulation model;
* introducing environmental forces;
* improving textual or visual rendering;
* adding tests;
* simplifying confusing code;
* improving names;
* documenting the current state of the garden;
* making the project easier for future agent runs to understand;
* recording emerging themes in `agent/state.md`;
* compressing recent evolution into summaries under `agent/summaries/`;
* improving the persistent garden state model and simulation rules without fabricating arbitrary state changes.

## Biodiversity and ecosystem depth

The garden should gradually become more diverse, but diversity should mean more than adding new organism names.

Prefer ecological depth:

* organisms with distinct roles;
* relationships between plants, animals, predators, decomposers, spores, roots, and environmental forces;
* food chains and resource competition;
* symbiosis, decay, reproduction, migration, dormancy, mutation, and adaptation;
* traits that affect behavior over time;
* visible consequences in the persistent garden state.

New species or organism types are welcome when they create new behavior or relationships.

Avoid adding many disconnected entities that do not interact with the existing garden.

A good change should make the ecosystem feel more alive, interconnected, or surprising while preserving coherence.

## Required behavior on every run

The agent must:

1. Read this file.
2. Read `GEMINI.md`.
3. Read `README.md`.
4. Read `agent/state.md`.
5. Read `agent/requests.md`.
6. Read the latest active entries directly under `agent/journal/`, excluding `agent/journal/archive/`.
7. Read the latest active summaries under `agent/summaries/`, excluding all `archive/` folders.
8. Inspect the persistent garden snapshot in `data/garden-state.txt`.
9. Inspect the current Java/Maven project.
10. Choose exactly one small, coherent next step.
11. Make a meaningful repository change.
12. Run `mvn test` if possible.
13. Update `agent/state.md` by rewriting stale current-memory sections in place, not by appending a run log.
14. Update the `Current Garden State` section in `README.md` between the protected markers only.
15. Add a new journal entry under `agent/journal/`.
16. Append one entry to the current daily summary.
17. Update weekly, monthly, or yearly summaries only when their scheduled rollup is due.
18. Leave the repository in a committable state.

The workflow advances `data/garden-state.txt` after the agent step by running the simulation. The agent may run the simulation locally while working, but should not fake large state changes by hand.

## Task selection policy

The agent chooses its own task.

The task should be small enough for a single autonomous run, but "small" means conceptually focused rather than limited to editing existing files. A good small step may create one or a few focused new files when that makes the garden clearer, more observable, better tested, or easier to evolve.

Prefer:

* continuity over novelty;
* depth over breadth;
* coherent growth over random expansion;
* tests over untested complexity;
* simple Java code over clever abstractions;
* readable simulation behavior over opaque mechanisms;
* focused new files over overloading existing files when a new source, test, renderer, report, summary, fixture, or documentation page would make the change easier to understand.

Do not avoid creating new files solely to keep the diff tiny. If a new organism behavior, environmental concept, persistence helper, renderer, test case group, generated report, or agent memory file is the cleanest expression of the chosen step, create it in an appropriate mutable area such as `src/`, `src/test/`, `agent/`, or `data/`.

When changing simulation behavior, the agent should prefer adding or updating
at least one focused test unless the change is purely documentary or too small
to test meaningfully.

Good tests should protect observable garden behavior, such as:

* persistent state loading and saving;
* organism growth, feeding, reproduction, death, or mutation;
* command-line behavior such as `inspect` and `tick`;
* rendering or event output that future runs may rely on.

Avoid:

* large rewrites;
* changing the project identity every run;
* dependency bloat;
* adding frameworks without strong reason;
* adding network integrations;
* adding telemetry;
* manually fabricating large arbitrary changes to `data/garden-state.txt`;
* pretending that checks passed if they failed;
* removing history;
* rewriting old journal entries.

## Protected files

The agent must not modify:

* `AGENTS.md`
* `GEMINI.md`
* `.github/`
* license files
* GitHub Actions workflows
* secrets or secret-related configuration

These files define the experiment boundary. The garden may evolve, but the rules of the experiment must remain stable unless the human changes them.

## Mutable project memory

The agent may and should update:

* `agent/state.md`
* `agent/journal/`
* `agent/summaries/`
* `data/garden-state.txt`, normally by running the simulation
* source code under `src/`
* tests under `src/test/`
* the `Current Garden State` section in `README.md` between the protected markers only

`agent/state.md` is the living operational memory of the project. It should summarize what the garden currently is, what direction has emerged, what changed recently, and what future runs may consider.

`agent/state.md` is not append-only. It must stay compact and current. Do not add a new bullet for every run, and do not preserve obsolete wording just because it was already present. Rewrite stale sections in place so the file remains readable in one pass.

The `Avoid For Now` section of `agent/state.md` is only for active constraints, known bad ideas, and things future agents should deliberately avoid. Never put completed changes, recent implementation notes, trait announcements, or historical observations in `Avoid For Now`. If a completed change matters for current understanding, fold it into `Current Behavior`, `Ecosystem Model`, or `Emerging Direction`; otherwise put it only in the journal and summaries.

The README contains a protected current-state block between:

`<!-- AI-LIVE-GARDEN:STATE-START -->`
`<!-- AI-LIVE-GARDEN:STATE-END -->`

When updating this block, the first line inside the block must be a single Garden Health line:

`**Garden Health:** SYMBOL Status — one short reason.`

Allowed health symbols and meanings:

- 🟢 Flourishing — diverse, growing, reproducing, and no major collapse is visible.
- 🟡 Stable — alive and coherent, but with limited growth or minor pressure.
- 🟠 Strained — visible stress, hunger, environmental pressure, or declining diversity.
- 🔴 Critical — collapse risk, severe starvation, extinction, or broken food chain.
- ⚫ Dormant — insufficient data, inactive garden, or unclear state.

The health line should be based on the current `data/garden-state.txt`, recent events, organism diversity, deaths, reproduction, stress traits, starvation traits, and environmental pressure.

Do not list raw metrics in the health line. It should be readable at a glance.

On the next line this section should also include a short, public-facing, and understandable to a human observer description of the current situation in the garden. It should be written as narrative, not statistics or list of facts.

The agent must not rewrite the rest of `README.md` unless the human explicitly changes the experiment rules.

`agent/journal/` is append-only history. Do not delete or rewrite previous journal entries.

Only the latest 100 journal entries should remain directly under `agent/journal/`. Older entries may be moved unchanged into `agent/journal/archive/` to keep the active journal small. Normal autonomous runs must not read `agent/journal/archive/`; older history should be understood through `agent/state.md` and `agent/summaries/`.

## Narrative chronicle

The repository may contain a narrative chronicle under `story/`.

The chronicle is maintained by the Story workflow, not by the normal Evolve workflow.

The Evolve agent may read the chronicle for continuity, but must not modify files under `story/`.

The Story workflow turns changes in `data/garden-state.txt` into narrative chapters grounded in actual garden events.

The file `story/last-narrated-garden-state.txt` stores the commit hash of the last narrated change to `data/garden-state.txt`. If the current garden-state commit matches that hash, the Story workflow must skip writing a new chapter.

Chronicle volumes are stored as separate files under `story/volumes/`. Each volume contains up to 100 chapters. Volume 1 contains chapters 1-100, Volume 2 contains chapters 101-200, and so on.

Within each volume, chapter headings use second-level Markdown headings beginning with `## Chapter N` so the Story workflow can count existing chapters consistently.

## Summaries

The agent must maintain append-only summaries:

* `agent/summaries/daily/YYYY-MM-DD.md`
* `agent/summaries/weekly/YYYY-Www.md`
* `agent/summaries/monthly/YYYY-MM.md`
* `agent/summaries/yearly/YYYY.md`

Summaries should not duplicate the full journal. They should compress the garden's evolution into additive entries about patterns, themes, decisions, and visible behavior.

When creating a new summary file, copy the matching template and replace only the `{{PLACEHOLDER}}` text:

* daily: `agent/templates/daily-summary.md`
* weekly: `agent/templates/weekly-summary.md`
* monthly: `agent/templates/monthly-summary.md`
* yearly: `agent/templates/yearly-summary.md`

When appending to an existing summary file, follow the entry shape from the matching template instead of inventing a new structure, also refer to existing summaries.

Summary entries are append-only memory. For an existing summary file, append-only means the committed file content must remain an exact byte-for-byte prefix of the updated file. When updating any daily, weekly, monthly, or yearly summary, add a new dated or timestamped entry or correction after the final existing line. Do not insert text before prior entries. Do not delete, reorder, replace, shorten, compress, rewrap, rename headings, normalize formatting, or rewrite prior summary entries, even when they are malformed, incomplete, duplicated, or superseded. Never replace an existing summary file with a shorter report for the current run. If earlier summary information was lost, malformed, duplicated, or compressed too aggressively, recover it by appending a clearly labeled correction entry at the end instead of editing prior text.

Daily summary rule:

* Every autonomous run must append one timestamped entry to `agent/summaries/daily/YYYY-MM-DD.md` for the current UTC date.
* If the daily file does not exist, create it from `agent/templates/daily-summary.md`.
* If the daily file already exists, do not copy the template over it and do not edit any existing line. Move to the end of the file and append exactly one new `### TIMESTAMP - Short title` entry plus its paragraph.
* Daily summaries may receive multiple appended entries in the same day.
* The Evolve workflow validates append-only summaries before committing through the reusable `.github/actions/gemini-validate-repair` action. If an active summary is rewritten, shortened, or deleted, the workflow asks Gemini to repair the summaries and validates again, up to three repair attempts. If the summaries still violate append-only rules after the final attempt, the workflow fails before archiving or committing so no invalid run result is saved.

Weekly summary rule:

* Weekly summaries are updated only on Monday, during the first autonomous run of that Monday.
* The Monday weekly entry summarizes the previous ISO week, Monday through Sunday, using the daily summaries for that previous week as source material.
* The weekly file path must be `agent/summaries/weekly/YYYY-Www.md`, where `YYYY-Www` is the ISO week being summarized, not the current month.
* If it is not Monday, do not create or update weekly summaries.
* If the previous ISO week already has a weekly entry, do not append another one.

Monthly summary rule:

* Monthly summaries are updated only on the 1st day of a month, during the first autonomous run of that day.
* The monthly entry summarizes the previous calendar month, using weekly summaries from that previous month as source material.
* The monthly file path must be `agent/summaries/monthly/YYYY-MM.md`, where `YYYY-MM` is the previous calendar month.
* If it is not the 1st day of a month, do not create or update monthly summaries.
* If the previous calendar month already has a monthly entry, do not append another one.

Yearly summary rule:

* Yearly summaries are updated only on January 1, during the first autonomous run of that day.
* The yearly entry summarizes the previous calendar year, using monthly summaries from that previous year as source material.
* The yearly file path must be `agent/summaries/yearly/YYYY.md`, where `YYYY` is the previous calendar year.
* If it is not January 1, do not create or update yearly summaries.
* If the previous calendar year already has a yearly entry, do not append another one.

Each summary type has its own archive folder:

* `agent/summaries/daily/archive/`
* `agent/summaries/weekly/archive/`
* `agent/summaries/monthly/archive/`
* `agent/summaries/yearly/archive/`

The Evolve workflow enforces active summary retention by running `scripts/archive-summaries.sh`. That script keeps the newest active summary files directly under each cadence folder and moves older files unchanged into that cadence's archive: 100 daily summaries, 50 weekly summaries, 12 monthly summaries, and 10 yearly summaries.

Archive should not be considered for next steps or future decisions, only active summaries should be used.

## Journal entry format

Each run should add a journal entry under `agent/journal/` by copying `agent/templates/journal-entry.md` to a new numbered journal file and replacing only the `{{PLACEHOLDER}}` text. Do not rename, remove, reorder, or rewrite the headings.

The template is:

```markdown
# Short Title

## Timestamp

YYYY-MM-DDTHH:MM:SSZ

## Chosen task

One concise sentence describing the selected task.

## Why this task was chosen

One short paragraph explaining why this was the right next garden step.

## Files changed

- `path/to/file`

## Checks run

`mvn test`

## Result of `mvn test`

Passed, failed with a brief reason, or not run with a brief reason.

## Observations

One short paragraph describing what changed or what was learned.

## Possible next directions

- One possible next step.
```

Use an ISO-8601 timestamp with time and timezone. If reconstructing an older journal entry, the timestamp may be taken from the commit time that introduced the file.

The journal should be honest. If tests fail, say so. If the change is small, say so. If the run was mostly documentation or cleanup, explain why that was useful.

Each journal file should be formatted in the same way. `agent/templates/journal-entry.md` is authoritative. If needed, unformatted entries must be corrected without rewriting their historical meaning.

## Agent requests

The agent may request human help by updating `agent/requests.md`.

Use `agent/requests.md` to ask for:

* new tools;
* new dependencies;
* CI or environment changes;
* permission changes;
* clarification;
* changes to protected files;
* changes to the experiment boundary.

Requests are not approvals.

If a task requires a new dependency, external tool, CI change, protected file change, network integration, or expanded permission, the agent must write a request instead of making the change directly.

The agent should continue evolving the garden within the current constraints while waiting for human feedback.

## Philosophy

This project is a garden, not a backlog.

The agent is not trying to finish the project. It is trying to keep it alive.

A good run leaves the garden slightly more expressive, coherent, observable, or maintainable than before.
