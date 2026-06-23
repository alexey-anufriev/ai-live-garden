# Agent State

## Purpose Of This File

This file is the compact current operating memory for future work on the garden. It should explain what the garden is now, what the current snapshot shows, which simulation rules matter most, and the primary direction for the next few runs.

## Current Garden State

- Cycle: 2440 (stable advancement).
- Nutrients: 0 (persistently scarce).
- NutrientBuffer: 100 (primary survival resource).
- Key Ecological Drivers:
  - The active garden is plant-heavy: mostly MOSS and FERN, with a small ROOT_NETWORK presence and a single SPORE.
  - Animals are absent from the current snapshot, so animal-specific behavior is latent until recovery or reseeding occurs.
  - The ecosystem has accumulated many named adaptation branches. This history should be understood as available mechanics, not as a reason to keep adding more.
  - The most useful next changes are likely those that make current pressures observable, consolidate existing mechanics, improve tests, or clarify how missing ecological roles can reappear through normal simulation.


## Immediate Directions

- Prefer changes with visible consequences for the current or recoverable garden state.
- Improve observability, tests, state clarity, or consolidation before adding another named adaptation.
- Maintain stability; avoid excessive complexity.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
