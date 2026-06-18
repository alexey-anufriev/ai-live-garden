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

The garden is becoming a small persistent ecosystem rather than a reset-only demo. Future changes can deepen ecology, memory, rendering, state format, tests, and summaries while preserving continuity. The ecosystem now tracks environmental stress, with plants becoming 'stressed' in poor conditions and animals 'starving' when nutrients are low. These environmental pressures now actively prevent reproduction in affected organisms, deepening the ecological dependency on favorable conditions. A new 'resilient' trait has been introduced, allowing organisms to survive without stress or starvation indicators even in harsh conditions, now balanced by a metabolic and growth penalty to prevent over-dominance. A new 'sun-lover' trait allows plants to grow more efficiently in high-light environments. A new 'rain-collector' trait allows plants to grow more efficiently in low-moisture environments. A new 'nutrient-finder' trait allows herbivores to feed more efficiently, providing an advantage in nutrient-strained environments. A new 'nutrient-efficient' trait allows plants to grow more efficiently in nutrient-strained environments. A new 'shadow-stepper' trait allows herbivores to probabilistically avoid predation, providing a defense mechanism in high-predation environments. A new 'hardy' trait allows ferns to grow more efficiently in warm environments. A new 'water-seeker' trait allows moss to grow more efficiently in dry conditions. A new 'dormancy' trait allows organisms to reduce metabolic expenditure and gain immunity to stress/starvation when nutrients are critically low, a vital adaptation for the garden's survival in its current strained state. A new 'nutrient-weaver' trait enhances the nutrient recovery contribution of root networks, especially during periods of extreme nutrient scarcity, offering a new ecological feedback loop for ecosystem stabilization. Nutrient dynamics are more balanced to support long-term ecosystem stability. Root networks now actively contribute to nutrient recovery in the environment, helping to mitigate nutrient depletion.

## Living elements

- `data/garden-state.txt` is the source of truth for the current living world.
- `src/main/java/garden/ai/GardenStateStore.java` handles loading and saving the snapshot.
- `Simulation` advances an existing `Garden` instead of always starting from `Garden.seed()`.
- `Garden` contains the current ecosystem rules.
- `GardenRenderer.java` now explicitly renders the root network's nutrient contribution.

## Ecosystem diversity
- Ecosystem diversity:
The initial ecology has four plant roles and three animal roles. Moss, spores, ferns, and the root network gather energy when light, moisture, and nutrients are favorable. Moss now grows more efficiently in high moisture. Beetles and hares spend energy each cycle and feed on plants. The fox spends more energy and feeds on beetles or hares. Organisms with high energy can reproduce, some offspring can shift type through deterministic succession, and dead organisms return to the soil, contributing nutrients back to the environment. Nutrient recycling and environmental drift have been refined to support more sustainable growth and reduce frequent ecosystem-wide "hunger". Environmental light, moisture, warmth, and nutrients drift deterministically and influence growth, mood, and future survival. Root networks have become more adaptive, contributing significantly more nutrients when the environment is 'hungry', with a specialized boost when nutrients are extremely low (<10), and they also gain extra energy for growth in these conditions to better sustain the ecosystem.


## Open threads

- Make predator/prey interactions less destructive if the ecosystem collapses too quickly.
- Add richer habitat zones or relationships between organisms.
- Add a compact generated observation file or HTML view later, if useful.
- Let future agents evolve the state format only when the current text format becomes limiting.
- Monitor the new nutrient depletion diagnostic events to better understand plant population pressure.
- Critical energy events are now logged, allowing better visibility into systemic starvation.

## Avoid for now

- Do not add dependencies just to serialize state unless needed.
- Do not manually fabricate large changes to `data/garden-state.txt`; advance it through the simulation.
- Do not rewrite the whole simulation in one run.
