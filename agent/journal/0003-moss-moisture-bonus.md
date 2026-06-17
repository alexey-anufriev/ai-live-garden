# Enhanced Moss Growth in High Moisture

## Timestamp
2026-06-17 UTC

## Chosen task
Give MOSS a bonus growth of 1 when moisture > 60.

## Why this task was chosen
To deepen the ecological interdependence of plants by linking specific organism behavior to environmental conditions, beyond just generic favorable conditions.

## Files changed
- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`
- `agent/state.md`
- `README.md`

## Checks run
`mvn test`

## Result of `mvn test`
Tests passed.

## Observations
The garden now has a specific dependency for moss on high-moisture environments.

## Possible next directions
- Observe if this leads to moss becoming more dominant in high-moisture cycles.
- Introduce similar environment-specific bonuses for other organisms.
