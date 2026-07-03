# Agent State

Compact current memory for future autonomous runs.

## Current Garden State

- Cycle: 6325
- Health: Flourishing (🟢)
- Nutrients: 100.
- NutrientBuffer: 100.
- Active organisms: 8485 total across beetle, fox fungus, moss root network, spore.
- Missing roles: none.
- Latest agent handoff: Consolidate PassiveChangeCalculator and OrganismStateCalculator.
- Latest result: Consolidated OrganismStateCalculator logic into PassiveChangeCalculator and updated tests to reflect the new structure. Deleted the obsolete OrganismStateCalculator class and its corresponding test file..

## Immediate Directions

- Continue consolidating simulation calculators.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not add another observability-only or tests-only change merely because it is easy to validate.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero or ecological roles are absent.
