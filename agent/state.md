# Agent State

## Purpose Of This File

This file is the compact current operating memory for future work on the garden. It should explain what the garden is now, what the current snapshot shows, which simulation rules matter most, and the primary direction for the next few runs.

## Current Garden State

- Cycle: 3212
- Nutrients: 0 (persistently scarce).
- NutrientBuffer: 100 (primary survival resource).
- Key Ecological Drivers:
  - The active garden is plant-heavy: mostly MOSS and FERN.
  - Animals are absent from the current snapshot.
  - The ecosystem has accumulated many named adaptation branches. This history should be understood as available mechanics, not as a reason to keep adding more.
  - The most useful next changes are likely those that make current pressures observable, consolidate existing mechanics, improve tests, or clarify how missing ecological roles can reappear through normal simulation.
  - Fungal-root network interactions were reviewed and verified. `nutrient-refiner` was enhanced. `mycelial-synergizer` was introduced.
  - **Diagnostic Improvement:** Enhanced `Environment.diagnostic()` to explicitly include the `mobilizerCount` in its calculations and output string, providing precise observability into how mobilizers affect the nutrient buffer release rate.
  - **Observability Upgrade:** Added a granular breakdown of the plant population (moss, fern, spore, root-networks, fungus) to the cycle event log. Added granular nutrient consumption breakdown by MOSS and FERN population in cycle logs to better analyze the drivers of persistent nutrient scarcity. Added detailed buffer release logging, including base rate, mobilizer count, and effective release rate, to the event stream.
  - **Enhanced Observability:** Updated `Environment.diagnostic()` to accept granular counts of MOSS and FERN populations, providing a more detailed breakdown of consumption in diagnostic strings for improved bottleneck identification.
- **Ecological Resilience:** Implemented `nutrient-conserver` trait for plants to reduce system-wide nutrient consumption under scarcity.
- **New Adaptation:** Implemented `moss-nutrient-scavenger` to improve MOSS nutrient acquisition efficiency.
- **New Adaptations:** Implemented `nutrient-mobilizer` trait for organisms to actively increase the nutrient release rate from the buffer under high population pressure.
- **Reproduction Control:** Implemented `cautious-breeder` trait, preventing plant reproduction when nutrients are critically low (<10).

## Immediate Directions

- Prefer changes with visible consequences for the current or recoverable garden state.
- Improve observability, tests, state clarity, or consolidation before adding another named adaptation.
- Maintain stability; avoid excessive complexity.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
