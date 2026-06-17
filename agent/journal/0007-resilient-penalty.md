# Metabolic penalty for resilient organisms

## Timestamp
2026-06-17 UTC

## Chosen task
Implement a metabolic/growth penalty for organisms with the 'resilient' trait.

## Why this task was chosen
The resilient trait introduced in the previous run provided a survival advantage without a cost, potentially leading to ecosystem imbalance. Adding a metabolic penalty balances this advantage and deepens the ecological simulation as suggested in the previous journal entry.

## Files changed
- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`
- `agent/state.md`
- `README.md`

## Checks run
`mvn test`

## Result of `mvn test`
Tests passed (11 tests run).

## Observations
The metabolic/growth penalty for the 'resilient' trait should now prevent it from being an overwhelmingly dominant survival strategy, forcing organisms to balance adaptability against higher energy demands.

## Possible next directions
- Monitor if the 'resilient' trait becomes too costly and is selected against in the long term.
- Further refine the metabolic cost to be dynamic based on environmental conditions.
