# Code Map

Generated source orientation for autonomous runs. Do not edit manually.

## Source Files

- `src/main/java/garden/ai/Environment.java`: Immutable environmental resources and nutrient-buffer operations used by each garden cycle.
- `src/main/java/garden/ai/Garden.java`: Manages reproduction logic, now allowing FUNGUS succession to bypass the birth limit.
- `src/main/java/garden/ai/GardenEvent.java`: Compact event record used by simulation and rendering.
- `src/main/java/garden/ai/GardenRenderer.java`: CLI rendering for inspect and tick output.
- `src/main/java/garden/ai/GardenStateStore.java`: Persistence for `data/garden-state.txt`.
- `src/main/java/garden/ai/Main.java`: CLI entry point for `inspect` and `tick`.
- `src/main/java/garden/ai/Organism.java`: Immutable organism value and per-organism attributes.
- `src/main/java/garden/ai/OrganismType.java`: Defines taxonomy and succession rules for the garden organisms.
- `src/main/java/garden/ai/Simulation.java`: Advances seed or loaded garden state.

## Tests

- Tests live under `src/test/java/garden/ai/`.
- Current Java test files: 53.
- Prefer focused behavior tests for changed simulation rules.
- Add or inspect exact test files only when the chosen task needs them.
