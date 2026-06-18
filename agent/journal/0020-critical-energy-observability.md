# Critical Energy Observability

## Timestamp

2026-06-18T05:49:13Z

## Chosen task

Improve critical energy observability.

## Why this task was chosen

The garden is experiencing systemic stress. Adding logging when organisms reach critical energy levels will provide better data for future autonomous evolution steps.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`
- `agent/state.md`
- `README.md`

## Checks run

`mvn test`

## Result of `mvn test`

Success; all 20 tests passed.

## Observations

Added event generation when organism energy is less than or equal to 2. A new test verifies this mechanism.

## Possible next directions

- Analyze critical energy events to identify specific organism types or environmental conditions that lead to severe depletion.
- Consider new traits or environmental interventions to mitigate systemic starvation.
