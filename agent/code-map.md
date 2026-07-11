# Code Map

Generated source orientation for autonomous runs. Do not edit manually.

## Source Files

- `src/main/java/garden/ai/Environment.java`: Immutable environmental resources and nutrient-buffer operations, now including feeding-based updates.
- `src/main/java/garden/ai/Garden.java`: Immutable garden snapshot, now delegating cycle advancement orchestration.
- `src/main/java/garden/ai/GardenEvent.java`: Compact event record used by simulation and rendering.
- `src/main/java/garden/ai/GardenRenderer.java`: CLI renderer, now utilizing TraitRegistry for trait counting.
- `src/main/java/garden/ai/GardenStateStore.java`: Persistence for `data/garden-state.txt`.
- `src/main/java/garden/ai/Main.java`: CLI entry point for `inspect` and `tick`.
- `src/main/java/garden/ai/Organism.java`: Represents a single living element in the garden, now utilizing TraitRegistry for nutrient value calculation.
- `src/main/java/garden/ai/OrganismInteractionCalculator.java`: Calculates organism interactions and reproduction, now including bypass logic for pioneer organisms.
- `src/main/java/garden/ai/OrganismType.java`: Defines taxonomy and environmental succession rules for organisms.
- `src/main/java/garden/ai/Simulation.java`: Advances seed or loaded garden state.
- `src/main/java/garden/ai/TraitRegistry.java`: Registry for trait definitions and effect calculations, now including the 'nutrient-pioneer' trait.

## Tests

- Tests live under `src/test/java/garden/ai/`.
- Current Java test files: 104.
- Prefer focused behavior tests for changed simulation rules.
- Add or inspect exact test files only when the chosen task needs them.
