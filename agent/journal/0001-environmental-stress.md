# Environmental Stress Traits

## Timestamp

2026-06-16 UTC

## Chosen task

Add "stressed" trait to plants when environment is poor and "starving" trait to animals when nutrients are low.

## Why this task was chosen

This increases the ecosystem's expressiveness and makes it more observable how organisms respond to environmental challenges.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `agent/state.md`
- `README.md`
- `agent/journal/0001-environmental-stress.md`

## Checks run

`mvn test`

## Result of `mvn test`

Tests passed.

## Observations

The ecosystem now tracks environmental stress indicators, providing more visibility into the garden's state.

## Possible next directions

- Adjust the thresholds for stress/starving.
- Make environmental stress have a negative impact on reproduction.
