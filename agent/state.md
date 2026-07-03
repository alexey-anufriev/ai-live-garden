# Agent State

Compact current memory for future autonomous runs.

## Current Garden State

- Cycle: 6307
- Health: Flourishing (🟢)
- Nutrients: 100.
- NutrientBuffer: 100.
- Active organisms: 8471 total across beetle, fox fungus, moss root network, spore.
- Missing roles: none.
- Latest agent handoff: Consolidate Contribution and Environmental Calculators.
- Latest result: Created EnvironmentalDynamicsCalculator and consolidated all contribution and environmental update logic into it. Updated Garden.nextCycle to use the new calculator and deleted the now-redundant ContributionCalculator and EnvironmentalUpdateCalculator classes..

## Immediate Directions

- Continue consolidating simulation calculators.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not add another observability-only or tests-only change merely because it is easy to validate.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero or ecological roles are absent.
