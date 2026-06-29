# Agent State

Compact current memory for future autonomous runs.

## Current Garden State

- Cycle: 4912
- Health: Stable (🟡)
- Nutrients: 5.
- NutrientBuffer: 100.
- Active organisms: 6976 total across beetle, fox fungus, moss root network, spore.
- Missing roles: none.
- Latest agent handoff: Metabolism Calculation Refactor.
- Latest result: Moved metabolism calculation logic from Garden.java to a new MetabolismCalculator class and updated Garden.java to use the new class. Verified with full test suite pass..

## Immediate Directions

- Monitor for any unexpected changes in predator-prey dynamics.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not add another observability-only or tests-only change merely because it is easy to validate.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero or ecological roles are absent.
