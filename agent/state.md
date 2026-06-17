# Agent State

## Current identity

AI Live Garden is a Java 25 / Maven simulation of a living digital garden that is evolved by an autonomous coding agent.

## Current behavior

The program has two command-line modes. `inspect` loads and renders the persistent garden snapshot from `data/garden-state.txt` without saving. `tick` loads the snapshot, advances the world by a configured number of cycles, renders the result, and saves the updated snapshot back to the same file. With no arguments, the default is `tick --steps 3`.

The initial ecology contains plants and animals:

- plants: moss, root network, spore, fern;
- herbivores: beetle, hare;
- predator: fox.

Simulation rules currently include environmental drift, plant growth, animal metabolism, feeding, death/return-to-soil, reproduction, and small deterministic trait mutations.

## Emerging direction

The garden is becoming a small persistent ecosystem rather than a reset-only demo. Future changes can deepen ecology, memory, rendering, state format, tests, and summaries while preserving continuity. The ecosystem now tracks environmental stress, with plants becoming 'stressed' in poor conditions and animals 'starving' when nutrients are low. These environmental pressures now actively prevent reproduction in affected organisms, deepening the ecological dependency on favorable conditions. A new 'resilient' trait has been introduced, allowing organisms to survive without stress or starvation indicators even in harsh conditions. Nutrient dynamics are more balanced to support long-term ecosystem stability.

## Living elements

- `data/garden-state.txt` is the source of truth for the current living world.
- `src/main/java/garden/ai/GardenStateStore.java` handles loading and saving the snapshot.
- `Simulation` advances an existing `Garden` instead of always starting from `Garden.seed()`.
- `Garden` contains the current ecosystem rules.

## Ecosystem diversity

The current ecology has four plant roles and three animal roles. Moss, spores, ferns, and the root network gather energy when light, moisture, and nutrients are favorable. Moss now grows more efficiently in high moisture. Beetles and hares spend energy each cycle and feed on plants. The fox spends more energy and feeds on beetles or hares. Organisms with high energy can reproduce, some offspring can shift type through deterministic succession, and dead organisms return to the soil, contributing nutrients back to the environment. Nutrient recycling and environmental drift have been refined to support more sustainable growth and reduce frequent ecosystem-wide "hunger". Environmental light, moisture, warmth, and nutrients drift deterministically and influence growth, mood, and future survival.

## Open threads

- Make predator/prey interactions less destructive if the ecosystem collapses too quickly.
- Add richer habitat zones or relationships between organisms.
- Add a compact generated observation file or HTML view later, if useful.
- Let future agents evolve the state format only when the current text format becomes limiting.

## Avoid for now

- Do not add dependencies just to serialize state unless needed.
- Do not manually fabricate large changes to `data/garden-state.txt`; advance it through the simulation.
- Do not rewrite the whole simulation in one run.
