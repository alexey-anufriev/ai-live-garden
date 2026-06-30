# Code Map

Generated source orientation for autonomous runs. Do not edit manually.

## Source Files

- `src/main/java/garden/ai/Environment.java`: Immutable environmental resources and nutrient-buffer operations used by each garden cycle.
- `src/main/java/garden/ai/FungalContributionCalculator.java`: Calculator for fungal network nutrient contributions.
- `src/main/java/garden/ai/Garden.java`: Simulation loop (updated to use FungalContributionCalculator).
- `src/main/java/garden/ai/GardenEvent.java`: Compact event record used by simulation and rendering.
- `src/main/java/garden/ai/GardenRenderer.java`: CLI renderer, now utilizing TraitRegistry for trait counting.
- `src/main/java/garden/ai/GardenStateStore.java`: Persistence for `data/garden-state.txt`.
- `src/main/java/garden/ai/Main.java`: CLI entry point for `inspect` and `tick`.
- `src/main/java/garden/ai/MetabolismCalculator.java`: Calculates organism metabolism, now using a consolidated ContributionContext.
- `src/main/java/garden/ai/Organism.java`: Represents a single living element in the garden, now utilizing TraitRegistry for nutrient value calculation.
- `src/main/java/garden/ai/OrganismType.java`: Defines organism taxonomy and now hosts environment-sensitive succession rules.
- `src/main/java/garden/ai/RootContributionCalculator.java`: Calculator for root network nutrient contributions.
- `src/main/java/garden/ai/Simulation.java`: Advances seed or loaded garden state.
- `src/main/java/garden/ai/TraitRegistry.java`: Central registry for trait-based behavior, now hosting trait counting methods.

## Tests

- Tests live under `src/test/java/garden/ai/`.
- Current Java test files: 58.
- Prefer focused behavior tests for changed simulation rules.
- Add or inspect exact test files only when the chosen task needs them.
