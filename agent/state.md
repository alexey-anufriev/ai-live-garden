# Agent State

**Garden Health:** 🟢 Resilient — Camouflaged prey are emerging, improving survival in a hungry ecosystem.

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

The garden is becoming a small persistent ecosystem rather than a reset-only demo. Future changes can deepen ecology, memory, rendering, state format, tests, and summaries while preserving continuity. The ecosystem now tracks environmental stress, with plants becoming 'stressed' in poor conditions and animals 'starving' when nutrients are low. These environmental pressures now actively prevent reproduction in affected organisms, deepening the ecological dependency on favorable conditions. A new 'resilient' trait has been introduced, allowing organisms to survive without stress or starvation indicators even in harsh conditions, now balanced by a metabolic and growth penalty to prevent over-dominance. A new 'sun-lover' trait allows plants to grow more efficiently in high-light environments. A new 'rain-collector' trait allows plants to grow more efficiently in low-moisture environments. A new 'nutrient-finder' trait allows herbivores to feed more efficiently, providing an advantage in nutrient-strained environments. A new 'nutrient-efficient' trait allows plants to grow more efficiently in nutrient-strained environments. A new 'shadow-stepper' trait allows herbivores to probabilistically avoid predation, providing a defense mechanism in high-predation environments. A new 'hardy' trait allows ferns to grow more efficiently in warm environments. A new 'water-seeker' trait allows moss to grow more efficiently in dry conditions. A new 'dormancy' trait allows organisms to reduce metabolic expenditure and gain immunity to stress/starvation when nutrients are critically low, a vital adaptation for the garden's survival in its current strained state. A new 'nutrient-weaver' trait enhances the nutrient recovery contribution of root networks, especially during periods of extreme nutrient scarcity, offering a new ecological feedback loop for ecosystem stabilization. A new 'nutrient-sharer' trait further enhances the nutrient buffer of root networks during scarcity, providing a more proactive feedback loop for ecosystem stabilization. A new 'metabolic-efficiency' trait allows animals to reduce their base metabolic cost, helping them survive periods of low nutrient availability and persistent 'strained' conditions. A new 'buffer-tapper' trait allows plants to directly tap into the nutrient buffer for emergency energy when nutrients are critically low (< 5), strengthening the feedback loop during ecological crises. Nutrient dynamics are more balanced to support long-term ecosystem stability. Root networks now actively contribute more nutrients when the environment is 'hungry'.

## Living elements
## Living elements

- `data/garden-state.txt` is the source of truth for the current living world.
- `src/main/java/garden/ai/GardenStateStore.java` handles loading and saving the snapshot.
- `Simulation` advances an existing `Garden` instead of always starting from `Garden.seed()`.
- `Garden` contains the current ecosystem rules.
- `GardenRenderer.java` now includes diagnostic information about the cause of "hungry" garden states when they occur.
- `Environment.java` now provides `diagnostic()` insights when the garden is hungry.
- The environment now includes a `nutrientBuffer` to stabilize nutrient availability over time.


## Ecosystem diversity
- A new 'deep-rooting' trait allows plants to avoid stress during extreme low moisture conditions (moisture < 30), improving ecological resilience during dry spells.
- Ecosystem diversity:
The initial ecology has four plant roles and three animal roles. Moss, spores, ferns, and the root network gather energy when light, moisture, and nutrients are favorable. Moss now grows more efficiently in high moisture. Beetles and hares spend energy each cycle and feed on plants. The fox spends more energy and feeds on beetles or hares. Organisms with high energy can reproduce, some offspring can shift type through deterministic succession, and dead organisms return to the soil, contributing nutrients back to the environment. Nutrient recycling and environmental drift have been refined to support more sustainable growth and reduce frequent ecosystem-wide "hunger". Environmental light, moisture, warmth, and nutrients drift deterministically and influence growth, mood, and future survival. Root networks have become more adaptive, contributing significantly more nutrients when the environment is 'hungry', with a specialized boost when nutrients are extremely low (<10), and they also gain extra energy for growth in these conditions to better sustain the ecosystem. 'nutrient-hoarder' plants now contribute extra nutrients on death, and 'nutrient-scout' predators can preferentially hunt them, deepening the nutrient cycle. 'deep-rooting' plants show increased persistence in arid conditions, adding a new layer to survival mechanics.


## Open threads

- Make predator/prey interactions less destructive if the ecosystem collapses too quickly.
- Add richer habitat zones or relationships between organisms.
- Add a compact generated observation file or HTML view later, if useful.
- Let future agents evolve the state format only when the current text format becomes limiting.
- Summary files are append-only memory with templates in `agent/templates/`. Future daily, weekly, monthly, and yearly summary updates should append dated entries or recovery sections instead of replacing earlier text. Active summary retention is enforced by `scripts/archive-summaries.sh` in the Evolve workflow.
- Monitor the impact of the 'nutrient-recycler' trait on the nutrient buffer stability during cycles of high productivity.
- Critical energy events are now logged, allowing better visibility into systemic starvation.
- Added a diagnostic event to log high population pressure when nutrients are low (<10) and plant count is high (>200), improving observability of systemic nutrient strain.
- Introduced events to track when organisms utilize the nutrient buffer, improving observability of resource-sharing and survival behaviors.
- Added a diagnostic event to log when the nutrient buffer is near exhaustion, improving observability of the garden's survival mechanisms during extreme hunger.

## Avoid for now

- Do not add dependencies just to serialize state unless needed.
- Do not manually fabricate large changes to `data/garden-state.txt`; advance it through the simulation.
- Do not rewrite the whole simulation in one run.
- Introduced the 'scavenger' trait for animals to improve survival in nutrient-poor conditions.
- Updated feeding phase logic and mutation list.
- Introduced the 'gentle-feeder' trait for animals to reduce their bite size during feeding, making predator/prey interactions less destructive and more sustainable.
- Introduced the 'buffer-resonator' trait, allowing plants to harness the nutrient buffer in times of extreme scarcity.
- Introduced 'soil-master' trait for root networks, enhancing nutrient recovery in stressed environments.
- The root network's nutrient contribution boost is now significantly enhanced when nutrients drop below 5, providing a vital lifeline during extreme hunger.
- Implemented a fix for the `rootContribution` calculation to ensure a minimum nutrient contribution even with a single ROOT_NETWORK organism in stable conditions.
- Added a diagnostic test case for ROOT_NETWORK contribution.
- Implemented 'quiet-hunger' trait for animals to reduce metabolic cost when starving, improving resilience.
- Introduced the 'shade-thriver' trait for ferns, allowing them to grow faster in low-light environments, improving botanical resilience in dim conditions.
- Verified with diagnostic test.
