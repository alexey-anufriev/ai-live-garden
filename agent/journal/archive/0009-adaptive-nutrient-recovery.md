# Adaptive Nutrient Recovery

## Timestamp

2026-06-17T17:46:49Z

## Chosen task

Introduce adaptive nutrient recovery for ROOT_NETWORK organisms.

## Why this task was chosen

The garden was persistently hungry, with nutrients at 0. Making nutrient recovery from root networks adaptive by doubling the effect when the environment is hungry introduces a feedback loop that stabilizes the ecosystem and deepens interdependence.

## Files changed

- `src/main/java/garden/ai/Environment.java`
- `agent/state.md`
- `README.md`

## Checks run

`mvn test`

## Result of `mvn test`

BUILD SUCCESS.

## Observations

Root networks now respond dynamically to environmental stress. The garden should be monitored to see if this keeps the nutrient level above 0 without causing excessive growth.

## Possible next directions

- Monitor the impact on long-term sustainability.
- Consider further refinement to nutrient consumption rates of other organisms.
