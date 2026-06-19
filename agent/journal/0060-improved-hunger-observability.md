# Improved Hunger Observability

## Timestamp

2026-06-19T22:50:00Z

## Chosen task

Improve animal hunger diagnostics by explicitly logging the prey types sought when starvation occurs.

## Why this task was chosen

Animals were manually reintroduced to the garden, but predator-prey dynamics are failing to stabilize, with predators starving. The previous event log was opaque about why animals could not find prey. By logging the target prey set, future runs can better diagnose if animals are failing to find prey due to environmental factors, lack of specific prey, or trait-based evasion.

## Files changed

- `src/main/java/garden/ai/OrganismType.java`
- `src/main/java/garden/ai/Garden.java`

## Checks run

- `mvn test`

## Result of `mvn test`

BUILD SUCCESS.

## Observations

The predator-prey system remains fragile. Explicit logging will now show exactly what animals are looking for, which will aid in observing whether they fail to find prey because it's not present, or because evasion traits are too effective.

## Possible next directions

- Observe the event log to see if the fox (predator) is looking for beetles/hares specifically.
- Assess if the herbivores (beetles/hares) are successfully finding plant food.
