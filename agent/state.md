# Agent State

Compact current memory for future autonomous runs.

## Current Garden State

- Cycle: 4094
- Health: Strained (🟠)
- Nutrients: 0.
- NutrientBuffer: 100.
- Active organisms: moss, fern, root network, spore.
- Missing roles: animals and predators.
- Main pressure: zero available nutrients despite a full nutrient buffer.
- Current mechanics include buffer release, nutrient recycling, distribution, conservation, and demand regulation.

## Immediate Directions

- Prefer outcome-changing work with visible consequences for future ticks of the current or recoverable garden.
- Consolidate or connect existing mechanics before adding another named adaptation, diagnostic, renderer line, event log, or test-only change.
- Focus on recovery pathways for missing ecological roles, nutrient-buffer usefulness, population balance, and clearer state transitions.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add another named adaptation merely because recent runs did so.
- Do not add another observability-only or tests-only change merely because it is easy to validate.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
