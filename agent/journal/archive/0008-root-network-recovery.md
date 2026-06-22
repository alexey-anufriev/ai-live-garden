# Root Network Nutrient Recovery

## Timestamp

2026-06-17T16:49:26Z

## Chosen task

Introduce ROOT_NETWORK nutrient recovery.

## Why this task was chosen

The garden was consistently in a "hungry" state with nutrients at 0. ROOT_NETWORK organisms were already in the system, and giving them a functional role in nutrient recovery adds ecological depth and improves system balance.

## Files changed

- `src/main/java/garden/ai/Environment.java`
- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`
- `agent/state.md`
- `README.md`

## Checks run

`mvn test`

## Result of `mvn test`

BUILD SUCCESS.

## Observations

ROOT_NETWORK organisms now help in nutrient cycling, which should help mitigate total nutrient depletion in future cycles.

## Possible next directions

- Monitor if this change stabilizes the garden.
- If the garden remains hungry, consider other nutrient sources or reducing nutrient demand of some organisms.
