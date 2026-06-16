# Agent State

## Current identity

AI Live Garden is a Java 25 / Maven simulation of a living digital garden that is evolved by an autonomous coding agent.

## Current behavior

The program loads the persistent garden snapshot from `data/garden-state.txt`, advances the world by a configured number of ticks, renders the result, and saves the updated snapshot back to the same file.

The initial ecology contains plants and animals:

- plants: moss, root network, spore, fern;
- herbivores: beetle, hare;
- predator: fox.

Simulation rules currently include environmental drift, plant growth, animal metabolism, feeding, death/return-to-soil, reproduction, and small deterministic trait mutations.

## Emerging direction

The garden is becoming a small persistent ecosystem rather than a reset-only demo. Future changes can deepen ecology, memory, rendering, state format, tests, and summaries while preserving continuity.

## Living elements

- `data/garden-state.txt` is the source of truth for the current living world.
- `src/main/java/garden/ai/GardenStateStore.java` handles loading and saving the snapshot.
- `Simulation` advances an existing `Garden` instead of always starting from `Garden.seed()`.
- `Garden` contains the current ecosystem rules.

## Open threads

- Make predator/prey interactions less destructive if the ecosystem collapses too quickly.
- Add richer habitat zones or relationships between organisms.
- Add a compact generated observation file or HTML view later, if useful.
- Let future agents evolve the state format only when the current text format becomes limiting.

## Avoid for now

- Do not add dependencies just to serialize state unless needed.
- Do not manually fabricate large changes to `data/garden-state.txt`; advance it through the simulation.
- Do not rewrite the whole simulation in one run.
