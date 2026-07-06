# Unmet Nutrient Demand Observability

## Timestamp

2026-06-25T19:55:00Z

## Chosen task

Implement unmet nutrient demand observability.

## Why this task was chosen

The garden faces chronic nutrient scarcity. While consumption breakdowns help, we lack a clear view of the *deficit*—how much more nutrient is needed to meet plant demand. Adding an 'unmet' demand metric provides this, enabling better ecological analysis of the scarcity bottleneck.

## Files changed

- `README.md`
- `agent/journal/0157-unmet-nutrient-demand-observability.md`
- `agent/state.md`
- `agent/summaries/daily/2026-06-25.md`
- `data/garden-state.txt`
- `src/main/java/garden/ai/Environment.java`
- `src/test/java/garden/ai/EnvironmentTest.java`

## Checks run

`mvn test`

## Result of `mvn test`

Passed: The final test run successfully validated the new observability metrics.

## Observations

The diagnostic now accurately identifies the unmet nutrient demand, making the scarcity bottleneck quantifiable and directly visible in the environment state render.

## Possible next directions

- Utilize this unmet demand metric to refine the `cautious-breeder` threshold or introduce new traits that respond specifically to unmet demand.
