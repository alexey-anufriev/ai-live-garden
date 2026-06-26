# Agent State

## Purpose Of This File

This file is the compact current operating memory for future work on the garden. It should explain what the garden is now, what the current snapshot shows, which simulation rules matter most, and the primary direction for the next few runs.

## Current Garden State

- Cycle: 3542
- Nutrients: 0 (persistently scarce).
- NutrientBuffer: 100 (primary survival resource).
- Key Ecological Drivers:
  - The active garden is plant-heavy: mostly MOSS and FERN.
  - Animals are absent from the current snapshot.
  - The ecosystem has accumulated many named adaptation branches. This history should be understood as available mechanics, not as a reason to keep adding more.
  - The most useful next changes are likely those that make current pressures observable, consolidate existing mechanics, improve tests, or clarify how missing ecological roles can reappear through normal simulation.
  - Fungal-root network interactions were reviewed and verified. `nutrient-refiner` was enhanced. `mycelial-synergizer` was introduced.
  - **Observability:** 
    - Added 'unmet demand' metric to `Environment.diagnostic()`, enabling real-time quantification of the nutrient deficit between plant demand and available supply.
    - Updated `Environment.diagnostic()` to include a granular breakdown of nutrient consumption alongside buffer release stats.
    - Formalized and documented the nutrient scarcity bottleneck behavior in `PlantBreakdownTest` to ensure future diagnostic enhancements accurately capture unmet demand under extreme population pressure.
    - Refactored `Garden` event logging in the feeding phase to aggregate organism death/recycling events, improving event history utilization and clarity while preserving essential aggregate nutrient and moisture recycling data.
    - Added `blockedPlantCount()` to report plants with reproduction blocked by 'stressed' or 'cautious-breeder' conditions, improving observability into the effectiveness of population regulation mechanisms during chronic nutrient scarcity.
    - Added buffer accumulation logging, tracking when the nutrient buffer is increasing to improve observability into surplus cycles.
    - Added comprehensive unit tests for `OrganismType.offspringType` to verify succession mechanics.
  - **Diagnostic Improvement:** `Environment.diagnostic()` was enhanced to calculate and report nutrient consumption reductions for MOSS and FERN populations separately, ensuring alignment with the internal simulation logic and improving diagnostic accuracy for population-specific consumption bottlenecks.
- **Ecological Resilience:** Implemented `nutrient-conserver` trait for plants to reduce system-wide nutrient consumption under scarcity.
- **New Adaptation:** Implemented `moss-nutrient-scavenger` to improve MOSS nutrient acquisition efficiency.
- **New Adaptations:** Implemented `nutrient-mobilizer` trait for organisms to actively increase the nutrient release rate from the buffer under high population pressure.
- **Reproduction Control:** Implemented `cautious-breeder` trait, preventing plant reproduction when nutrients are critically low (<10). Enhanced observability in the environment diagnostic to report the number of plants currently experiencing blocked reproduction.

## Immediate Directions

- Prefer changes with visible consequences for the current or recoverable garden state.
- Improve observability, tests, state clarity, or consolidation before adding another named adaptation.
- Maintain stability; avoid excessive complexity.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
