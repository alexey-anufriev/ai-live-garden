# Reproduction Restriction for Stressed/Starving Organisms

## Timestamp

2026-06-17T05:09:20Z

## Chosen task

Make environmental stress (stressed/starving) actively prevent reproduction.

## Why this task was chosen

This deepens the ecological impact of environmental stress, making organisms more dependent on favorable conditions to reproduce.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`
- `agent/state.md`

## Checks run

`mvn test`

## Result of `mvn test`

Tests passed.

## Observations

The garden now has a clear mechanism where environmental hardship restricts population growth, increasing the challenge for organisms to adapt and thrive.

## Possible next directions

- Adjust the thresholds for stress/starving.
- Observe how this impacts long-term population stability and if it leads to ecosystem collapse in poor environments.
