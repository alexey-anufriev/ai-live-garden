# Code Map

Generated source orientation for autonomous runs. Do not edit manually.

## Source Files

- `src/main/java/garden/ai/Environment.java`: Environment drift and nutrient buffer release logic updated to support buffer-release-accelerator.
- `src/main/java/garden/ai/Garden.java`: Immutable garden snapshot, now delegating cycle advancement orchestration.
- `src/main/java/garden/ai/GardenEvent.java`: Compact event record used by simulation and rendering.
- `src/main/java/garden/ai/GardenRecovery.java`: class GardenRecovery.
- `src/main/java/garden/ai/GardenRenderer.java`: Diagnostic output updated to include acceleratorCount.
- `src/main/java/garden/ai/GardenStateStore.java`: Persistence for `data/garden-state.txt`.
- `src/main/java/garden/ai/Main.java`: CLI entry point for `inspect` and `tick`.
- `src/main/java/garden/ai/Organism.java`: Represents a single living element in the garden, now utilizing TraitRegistry for nutrient value calculation.
- `src/main/java/garden/ai/OrganismInteractionCalculator.java`: Calculates organism interactions, now with stricter nutrient-dependent birth limits for foxes.
- `src/main/java/garden/ai/OrganismType.java`: Defines taxonomy and environmental succession rules, updated to lower SPORE-to-FUNGUS colonization threshold.
- `src/main/java/garden/ai/Simulation.java`: Advances seed or loaded garden state.
- `src/main/java/garden/ai/SimulationMetrics.java`: class SimulationMetrics, record Snapshot, record Report.
- `src/main/java/garden/ai/SimulationRandom.java`: class SimulationRandom.
- `src/main/java/garden/ai/TraitRegistry.java`: Centralized registry for metabolic and interaction traits; contains the optimized findPreyIndex method.

## Tests

- Tests live under `src/test/java/garden/ai/`.
- Current Java test files: 139.
- Prefer focused behavior tests for changed simulation rules.
- Add or inspect exact test files only when the chosen task needs them.
