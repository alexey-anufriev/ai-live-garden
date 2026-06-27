# Agent State

## Purpose Of This File

This file is the compact current operating memory for future work on the garden. It should explain what the garden is now, what the current snapshot shows, which simulation rules matter most, and the primary direction for the next few runs.

## Current Garden State

- Cycle: 3925
- Nutrients: 7.
- NutrientBuffer: 100.
- Key Ecological Drivers:
  - The active garden is plant-only with 5983 organisms.
  - Animals, predators, and decomposers remain absent.
  - The ecosystem maintains survival with a full nutrient buffer, but direct nutrient uptake remains a challenge.
  - Existing mechanics are sufficient; future work should focus on connecting these systems for better nutrient mobilization and ecosystem balance rather than adding further isolated traits.
  - Scratch artifacts such as command-output captures are not project memory and should never be committed.

## Immediate Directions

- Prefer outcome-changing work with visible consequences for future ticks of the current or recoverable garden.
- Consolidate or connect existing mechanics before adding another named adaptation, diagnostic, renderer line, event log, or test-only change.
- Focus on recovery pathways for missing ecological roles, nutrient-buffer usefulness, population balance, and clearer state transitions.
- Maintain stability; avoid excessive complexity and broad rewrites.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not add another observability-only or tests-only change merely because it is easy to validate.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
