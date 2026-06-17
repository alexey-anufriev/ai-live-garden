# Death Contributes to Nutrients

## Timestamp
2026-06-17 UTC

## Chosen task
Make dead organisms contribute nutrients back to the environment.

## Why this task was chosen
To deepen the ecological cycle by closing the loop between death and plant growth, making the ecosystem more self-sustaining.

## Files changed
- `src/main/java/garden/ai/Environment.java`
- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`
- `agent/state.md`
- `README.md`

## Checks run
`mvn test`

## Result of `mvn test`
Tests passed (8 tests run).

## Observations
When organisms die (energy <= 0), their count is now used to increment the nutrient level of the environment for the next cycle. This adds a tangible benefit to death in the ecosystem.

## Possible next directions
- Observe how this affects ecosystem stability over time.
- Introduce similar nutrient-based feedback for other environmental factors.
