# ai-live-garden

`ai-live-garden` is an autonomous software evolution experiment.

A Java/Maven codebase acts as a tiny digital garden. It contains organisms, environmental conditions, simulation cycles, events, and a project memory. A Gemini-powered coding agent is scheduled to wake up every 8 minutes, inspect the repository, choose one small next step, edit the code or documentation, run checks, and commit directly to `main`.

The goal is not to build a normal product. The goal is to observe how an AI coding agent expresses continuity, taste, priorities, and direction through a living repository.

## Current seed

The initial project is intentionally small:

- Java 25
- Maven
- JUnit 6 / AssertJ
- a deterministic garden simulation with plants, herbivores, predators, feeding, reproduction, and mutation
- persistent living state in `data/garden-state.txt`
- text rendering for the garden state
- agent memory in `agent/state.md`
- agent journal in `agent/journal/`
- GitHub Actions workflow in `.github/workflows/autonomous-garden.yml`

The workflow runs on this schedule:

```yaml
- cron: "*/8 * * * *"
```

This means minute `0,8,16,24,32,40,48,56` of every hour in UTC. GitHub Actions schedules are not guaranteed to run exactly on time, so missed or delayed runs are normal.

## Run locally

```bash
mvn test
mvn -q exec:java -Dexec.args="inspect"
mvn -q exec:java -Dexec.args="tick --steps 3"
```

Or build and run the jar:

```bash
mvn package
java -jar target/ai-live-garden-0.1.0-SNAPSHOT.jar
```

## Java code map

The Java implementation is deliberately small and dependency-light:

- `Main` is the command-line entry point. It supports `inspect` to render the current snapshot without saving, and `tick` to advance and persist the garden. Use `--steps N` to control cycle count and `--state path/to/file` to use an alternate snapshot.
- `Garden` is the immutable world snapshot and owns the ecosystem rules for each cycle: environmental drift, passive growth/metabolism, feeding, death, reproduction, mutation, and event trimming.
- `Organism`, `OrganismType`, `Environment`, and `GardenEvent` are the core value model. They keep state validation close to the data and expose small helper methods used by the simulation rules.
- `Simulation` advances either the seed garden or an existing loaded garden for a fixed number of cycles.
- `GardenStateStore` is the persistence boundary for `data/garden-state.txt`. Its line-oriented format is intended to remain readable in git diffs and easy for future agents to evolve.
- `GardenRenderer` turns a garden snapshot into the terminal text shown by local commands and autonomous runs.

## Repository memory

The agent should treat the repository itself as memory:

- `AGENTS.md` - universal agent rules.
- `GEMINI.md` - Gemini-specific instructions.
- `agent/state.md` - current state and direction.
- `agent/requests.md` - requests from the agent to the human observer.
- `agent/journal/` - chronological notes from autonomous runs.
- `agent/summaries/` - daily, weekly, monthly, and yearly compressed memory.
- `data/garden-state.txt` - the persistent living garden snapshot.

## Safety model

The first version intentionally commits directly to `main` to preserve the feeling of a live process. Safety is handled by constraints rather than PR review:

- the workflow uses only the repository-scoped `GITHUB_TOKEN`;
- the workflow grants only `contents: write`;
- the agent is instructed not to modify `.github/workflows`;
- the workflow restores `.github/workflows` after the agent step;
- no personal access token is required;
- no SSH key is required;
- secrets must never be committed.

This is still an experiment. Do not store valuable secrets or production code in this repository.

## Suggested observation questions

- Does the agent preserve continuity between runs?
- Does the garden become more coherent or more chaotic?
- Does the agent prefer code, documentation, tests, architecture, or narrative?
- Does the agent invent stable concepts?
- Does the agent repair its own broken changes?

## Current Garden State

<!-- AI-LIVE-GARDEN:STATE-START -->

The garden now has a committed persistent snapshot in `data/garden-state.txt`. The initial living state contains moss, a root network, a spore, a fern, a beetle, a hare, and a fox. Each simulation tick loads the snapshot, advances plants, grazers, predators, feeding, reproduction, and small trait mutations, then saves the updated world back into the repository.

<!-- AI-LIVE-GARDEN:STATE-END -->
