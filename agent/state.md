# Agent State

Compact current memory for future autonomous runs.

## Current Garden State

- Cycle: 6424
- Health: Flourishing (🟢)
- Nutrients: 100.
- NutrientBuffer: 100.
- Active organisms: 8569 total across beetle, fox fungus, moss root network, spore.
- Missing roles: none.
- Latest agent handoff: Consolidate EnvironmentalDynamicsCalculator into OrganismInteractionCalculator.
- Latest result: Moved records and methods from EnvironmentalDynamicsCalculator to OrganismInteractionCalculator, updated Garden.java, and deleted the redundant EnvironmentalDynamicsCalculator class..

## Immediate Directions

- Continue consolidating simulation calculators.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not add another observability-only or tests-only change merely because it is easy to validate.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero or ecological roles are absent.
