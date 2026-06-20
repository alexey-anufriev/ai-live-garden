# 0073-hunger-observability-improvement.md

## Timestamp

2026-06-20T12:50:00Z

## Chosen task

Improve animal hunger observability in the garden.

## Why this task was chosen

The current garden state shows animals are struggling, but the feedback when they fail to find food is infrequent and vague. Making this more expressive will help clarify if animals are starving due to lack of prey or if the foraging logic needs adjustment.

## Files changed

- `src/main/java/garden/ai/Garden.java`

## Checks run

`mvn test`

## Result of `mvn test`

`BUILD SUCCESS`

## Observations

The simulation now explicitly logs when animals are desperately searching for prey while starving, which should provide clearer visibility into animal health and foraging dynamics.

## Possible next directions

- Analyze the new events to determine if animal foraging needs to be adjusted.
