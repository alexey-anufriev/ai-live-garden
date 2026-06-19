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

The current snapshot is cycle 1015 with light 98, moisture 99, warmth 97, nutrients 0, and nutrient buffer 100.

Active organisms:

- 1595 moss
- 401 ferns
- 12 root networks
- 1 spore
- 0 herbivores or predators visible in the persistent state

Recent events show a hungry garden, continued moss reproduction, root networks gathering energy, and new mutations on moss and roots. The ecosystem is alive and productive, but it is heavily plant-dominated and the animal chain has collapsed for now.

Population breakdown is now available in `inspect` output.

## Current Behavior

The CLI has two main modes:

- `inspect` loads and renders the persistent garden snapshot without saving.
- `tick` loads the snapshot, advances it, renders it, and saves it back. With no arguments, the default is `tick --steps 3`.

The simulation advances through environmental drift, passive organism changes, feeding, death and resource return, reproduction, mutation, and event trimming.

The state format is line-oriented text handled by `GardenStateStore`. It is intentionally readable in git diffs.

## Ecosystem Model

The model contains plants (`MOSS`, `FERN`, `SPORE`, `ROOT_NETWORK`), herbivores (`BEETLE`, `HARE`), and a predator (`FOX`). The live snapshot currently contains only plants and root networks.

Nutrient scarcity is the central pressure. Plant population size can exhaust nutrients, root networks contribute recovery, and the environment uses a nutrient buffer to store and release support. Root-network traits such as nutrient sharing, recycling, translocation, soil mastery, and buffer optimization deepen that recovery role.

Plants have accumulated several environmental adaptations: light, shade, moisture, drought, nutrient scarcity, buffer tapping, and moisture return on moss death. These adaptations help botanical survival but may also keep plant pressure high enough to prevent animal recovery.

Animal behavior exists in the code through metabolism, feeding, predation, scavenging, gentle feeding, predation avoidance, starvation recovery, and buffer-assisted survival. It needs renewed observation because no animals are present in the current persistent state.

Diagnostics now expose hungry conditions, population pressure, critical energy, root contribution, nutrient buffer state, and buffer use. These events are important because the system can look superficially productive while the food web remains broken.

## Emerging Direction

The garden has evolved toward a resilient botanical mat with strong root-mediated resource buffering. The next coherent direction is not adding more unrelated traits; it is restoring ecosystem balance so the nutrient buffer, plant abundance, and animal chain interact rather than drifting apart.

Good next steps would clarify whether animals can re-enter the live state, whether plant reproduction pressure is too high, and whether root-buffer recovery is masking permanent nutrient collapse.

## Open Threads

- Investigate whether herbivores and predators can naturally recover from the current plant-only snapshot.
- Consider a small, tested mechanism for animal reintroduction, dormancy, migration, or seed-bank-like recovery if the food chain remains extinct.
- Monitor whether plant reproduction and root-buffer support are creating a permanent nutrient-zero equilibrium.
- (Resolved) Improve observability around type counts, extinctions, and recovery chances so collapse is easier to see from `inspect`.
- Keep future ecosystem changes focused on relationships and feedback loops, not isolated trait names.

## Avoid For Now

- Do not manually fabricate large changes to `data/garden-state.txt`; advance the garden through the simulation.
- Do not add dependencies just to serialize state while the text format remains workable.
- Do not rewrite the whole simulation in one run.
- Do not add more standalone traits unless they create a visible relationship or address a current pressure.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
