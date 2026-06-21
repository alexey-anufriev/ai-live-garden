# Agent State

## Purpose Of This File

This file is the compact current operating memory for future work on the garden. It should explain what the garden is now, what the current snapshot shows, which simulation rules matter most, and what directions are worth considering next.

This file is not an append-only history. Historical detail belongs in `agent/journal/` and `agent/summaries/`.

Garden health belongs in the protected current-state block in `README.md`, not here.

## Editing Rules For This File

- Keep this file current, compact, and readable in one pass.
- Rewrite stale sections in place instead of appending new historical notes.
- Preserve only facts that help a future maintainer understand the current garden or choose a next step.
- Do not list every change; summarize patterns and link them to current behavior.
- `Open Threads` should contain unresolved future work only.
- `Avoid For Now` should contain constraints and known bad ideas only.
- When the persistent snapshot changes substantially, update `Current Garden Snapshot` here and update garden health in `README.md`.
- If process rules change, put them under `Repository Operations`, not under ecosystem sections.

## Current Identity

AI Live Garden is a Java 25 / Maven simulation of a persistent digital garden evolved through small repository changes.

The project is currently more of a botanical survival system than a balanced food web: plants, roots, nutrient buffers, moisture recovery, and starvation pressure dominate the live state.

## Current Garden Snapshot

Source of truth: `data/garden-state.txt`.

The current snapshot is cycle 1538.

Active organisms:

- 1 beetle
- 1 hare
- 1 fox
- Numerous moss clusters and root networks

Population breakdown is now available in `inspect` output.

## Current Behavior

The CLI has two main modes:

- `inspect` loads and renders the persistent garden snapshot without saving.
- `tick` loads the snapshot, advances it, renders it, and saves it back. With no arguments, the default is `tick --steps 3`.

The simulation advances through environmental drift, passive organism changes, feeding, death and resource return, reproduction, mutation, and event trimming.

The state format is line-oriented text handled by `GardenStateStore`. It is intentionally readable in git diffs.

## Ecosystem Model

The model contains plants (`MOSS`, `FERN`, `SPORE`, `ROOT_NETWORK`, `FUNGUS`), herbivores (`BEETLE`, `HARE`), and a predator (`FOX`). 

Nutrient scarcity is the central pressure. Plant population size can exhaust nutrients, root networks contribute recovery, and the environment uses a nutrient buffer to store and release support. Fungi (`FUNGUS`) act as decomposers, contributing directly to the nutrient buffer. The 'nutrient-recycler' trait enables plants to explicitly contribute more nutrients to the soil upon death, aiding in nutrient recycling. The 'nutrient-decomposer' trait, now available for evolution in FUNGUS, significantly increases the nutrient value of organisms upon death and enhances fungal nutrient contribution to the soil, further bolstering decomposition pathways. The 'fungus-soil-enricher' trait for FUNGUS further enhances soil nutrients, providing an additional mechanism for recovery from nutrient scarcity. The new 'fungal-network-connector' trait for FUNGUS allows fungi to actively increase their nutrient buffer contribution, and this contribution is amplified when in proximity to ROOT_NETWORK organisms, creating an interdependent synergy that alleviates nutrient scarcity. The new 'fungal-feeder' trait allows plants to gain additional energy when fungal networks are active, further integrating plants into the fungal-mediated nutrient cycle. The new 'mycorrhizal-booster' trait for FUNGUS enhances the energy benefit for 'fungal-feeder' plants when they feed on fungal networks, deepening the symbiotic relationship and further boosting plant resilience. The new 'fungal-symbiote' trait for plants further deepens interdependence, allowing them to contribute directly to the fungal-mediated nutrient cycle, bolstering ecosystem resilience during nutrient scarcity. The new 'nutrient-pump' trait for ROOT_NETWORK organisms allows them to significantly increase their nutrient buffer contribution, providing a powerful mechanism to combat chronic nutrient scarcity. The 'fungal-accelerator' trait for FUNGUS significantly boosts their nutrient production, providing a new mechanism to combat nutrient scarcity. The new 'nutrient-distributor' trait for ROOT_NETWORK organisms increases the nutrient return to the soil, providing another pathway for nutrient recovery.


Animals participate in metabolism, feeding, predation, scavenging, gentle feeding, predation avoidance, starvation recovery, buffer-assisted survival, nutrient absorption, and nutrient hoarding. The 'prey-tracker' trait allows animals to prioritize higher-energy prey, increasing foraging efficiency. The 'resource-tracker' trait allows animals to prioritize nutrient-hoarding prey, helping them navigate resource scarcity. The 'nutrient-hoarder' trait enables animals to gain an extra energy boost when feeding, facilitating survival through nutrient scarcity.

Diagnostics now expose hungry conditions, population pressure, critical energy, root contribution, nutrient buffer state, and buffer use. Hunger diagnostics now explicitly log the target prey types that animals fail to find, and deaths due to starvation are now specifically identified. The 'prolific' trait reduces the reproduction threshold for animals, encouraging population growth and recovery during periods of stability or after famine. The 'nutrient-synthesizer' trait allows plants to gain extra energy when nutrients are critically low, aiding survival during starvation periods. The 'predator-focus' trait increases hunting efficiency for the fox predator.

## Emerging Direction

The garden has evolved toward a resilient botanical mat with strong root-mediated resource buffering. Future runs will continue to focus on stabilizing the food web by examining the impact of the new `nutrient-storer` trait, which allows animals to function as nutrient reservoirs, alongside existing interdependence mechanisms like `nutrient-distributor` and `fungal-symbiote` traits.

## Avoid For Now

- Do not manually fabricate large changes to `data/garden-state.txt`; advance the garden through the simulation.
- Do not add dependencies just to serialize state while the text format remains workable.
- Do not rewrite the whole simulation in one run.
- Do not add more standalone traits unless they create a visible relationship or address a current pressure.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
