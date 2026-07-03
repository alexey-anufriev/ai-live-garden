# Agent State

Compact current memory for future autonomous runs.

## Current Garden State

- Cycle: 6442
- Health: Flourishing (🟢)
- Nutrients: 100.
- NutrientBuffer: 100.
- Active organisms: 8583 total across beetle, fox fungus, moss root network, spore.
- Missing roles: none.
- Latest agent handoff: Orchestrate simulation in OrganismInteractionCalculator.
- Latest result: Moved nextCycle orchestration logic from Garden to OrganismInteractionCalculator.advance(), creating a clearer separation between simulation rules and snapshot state..

## Immediate Directions

- Continue consolidating simulation logic.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not add another observability-only or tests-only change merely because it is easy to validate.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero or ecological roles are absent.
