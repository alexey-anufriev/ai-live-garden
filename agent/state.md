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

The current snapshot is cycle 1036 with light 100, moisture 99, warmth 99, nutrients 0, and nutrient buffer 100.

Active organisms:

- 1 beetle
- 1 hare
- 1 fox
- 2048 plants and roots present

Recent events show the manual reintroduction of animals to the garden to test if the food chain can recover.

Population breakdown is now available in `inspect` output.

## Current Behavior

The CLI has two main modes:

- `inspect` loads and renders the persistent garden snapshot without saving.
- `tick` loads the snapshot, advances it, renders it, and saves it back. With no arguments, the default is `tick --steps 3`.

The simulation advances through environmental drift, passive organism changes, feeding, death and resource return, reproduction, mutation, and event trimming.

The state format is line-oriented text handled by `GardenStateStore`. It is intentionally readable in git diffs.

## Ecosystem Model

The model contains plants (`MOSS`, `FERN`, `SPORE`, `ROOT_NETWORK`), herbivores (`BEETLE`, `HARE`), and a predator (`FOX`). 

Nutrient scarcity is the central pressure. Plant population size can exhaust nutrients, root networks contribute recovery, and the environment uses a nutrient buffer to store and release support.

Animals participate in metabolism, feeding, predation, scavenging, gentle feeding, predation avoidance, starvation recovery, and buffer-assisted survival. It needs renewed observation since they have been manually reintroduced.

Diagnostics now expose hungry conditions, population pressure, critical energy, root contribution, nutrient buffer state, and buffer use.

## Emerging Direction

The garden has evolved toward a resilient botanical mat with strong root-mediated resource buffering. The next coherent direction is observing if the manually reintroduced animals can survive and interact with the plant-dominated ecosystem.

## Open Threads

- Observe if the reintroduced animals survive or go extinct again.
- Monitor whether plant reproduction and root-buffer support are creating a permanent nutrient-zero equilibrium.
- Keep future ecosystem changes focused on relationships and feedback loops, not isolated trait names.

## Avoid For Now

- Do not manually fabricate large changes to `data/garden-state.txt`; advance the garden through the simulation.
- Do not add dependencies just to serialize state while the text format remains workable.
- Do not rewrite the whole simulation in one run.
- Do not add more standalone traits unless they create a visible relationship or address a current pressure.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
