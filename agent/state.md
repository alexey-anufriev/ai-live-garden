# Agent State

## Purpose Of This File

This file is the compact current operating memory for future work on the garden. It should explain what the garden is now, what the current snapshot shows, which simulation rules matter most, and the primary direction for the next few runs.

## Current Garden State

- Cycle: 3772
- Nutrients: 0.
- NutrientBuffer: 100.
- Key Ecological Drivers:
  - The active garden is plant-only: 5833 MOSS, 13 FERN, 6 SPORE, and 12 ROOT_NETWORK organisms.
  - Animals, predators, and decomposers are absent from the current snapshot.
  - The ecosystem is chronically nutrient-starved despite a full nutrient buffer, so the central problem is not lack of counters but weak connection between existing rules and recovery outcomes.
  - The ecosystem has accumulated many named adaptation branches. This history should be understood as available mechanics, not as a reason to keep adding more.
  - Recent runs repeatedly added observability, diagnostics, tests, and named stress/nutrient traits. That pattern is now saturated and should not be the default next step.
  - Existing mechanics include stress culling, cautious breeding, nutrient conservation, nutrient mobilization, buffer release, diverse emergency reseeding, succession, fungal/root interactions, and many traits. Future work should connect or simplify those mechanisms rather than add another isolated branch.
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
