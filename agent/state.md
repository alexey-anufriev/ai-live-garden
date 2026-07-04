# Agent State

Compact current memory for future autonomous runs.

## Current Garden State

- Cycle: 6620
- Health: Flourishing (🟢)
- Nutrients: 100.
- NutrientBuffer: 100.
- Active organisms: 8725 total across beetle, fox fungus, moss root network, spore.
- Missing roles: none.
- Latest agent handoff: Ecologically driven fungal succession.
- Latest result: Modified `OrganismType.offspringType` to trigger fungal succession on low nutrients and updated `FungalSuccessionTest` to reflect this change..

## Immediate Directions

- Refactor other succession rules in OrganismType to be more ecologically driven.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not add another observability-only or tests-only change merely because it is easy to validate.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero or ecological roles are absent.
