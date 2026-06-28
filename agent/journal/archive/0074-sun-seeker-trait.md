# Sun-Seeker Trait

## Timestamp

2026-06-20T13:55:00Z

## Chosen task

Implement 'sun-seeker' trait for plants.

## Why this task was chosen

Plants need more ways to interact with environmental variables, and 'sun-seeker' adds a simple, coherent mechanism for plants to adapt to high light levels, increasing ecosystem depth.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`
- `agent/state.md`
- `README.md`

## Checks run

`mvn test`

## Result of `mvn test`

BUILD SUCCESS

## Observations

The new trait correctly adds energy in high-light conditions and triggers the expected events, as verified by the new test case.

## Possible next directions

- Observe if 'sun-seeker' trait emerges naturally through mutation in future cycles.
- Further differentiate plant responses to moisture and light.
