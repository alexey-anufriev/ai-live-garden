# Agent State

## Purpose Of This File

This file is the compact current operating memory for future work on the garden. It should explain what the garden is now, what the current snapshot shows, which simulation rules matter most, and the primary direction for the next few runs.

## Current Garden State

- Cycle: 4015
- Health: Strained (🟠)
- Nutrients: 1.
- NutrientBuffer: 100.
- Key Ecological Drivers:
  - The active garden is plant-only.
  - The 'buffer-releaser' trait is implemented to enhance nutrient mobilization from the nutrient buffer.
  - The 'nutrient-recycler' trait now synergizes with 'buffer-releaser' and actively accelerates nutrient release from the nutrient buffer, improving nutrient mobilization.
  - The 'nutrient-demand-regulator' trait for ROOT_NETWORK organisms is implemented to actively reduce the overall plant nutrient consumption, helping to balance demand during scarcity.
  - The nutrient mobilization feedback loop now includes supply-side (releasers/distributors) and demand-side (regulators) mechanisms.

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
