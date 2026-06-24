# Nutrient Consumption Breakdown Observability

## Timestamp

2026-06-24T16:50:00Z

## Chosen task

Improve observability of nutrient consumption by plant type (MOSS, FERN) to better understand persistent nutrient scarcity.

## Why this task was chosen

Nutrient scarcity is persistently high in the garden, but the current event logs only provide a total nutrient consumption figure. Breaking this down by plant type (moss, fern) will reveal how much each category contributes to consumption, aiding in diagnosing why scarcity persists and how adaptations affect it.

## Files changed

- `README.md`
- `agent/journal/0142-nutrient-consumption-breakdown-observability.md`
- `agent/state.md`
- `agent/summaries/daily/2026-06-24.md`
- `data/garden-state.txt`
- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/PlantBreakdownTest.java`

## Checks run

`mvn test -Dtest=PlantBreakdownTest`

## Result of `mvn test`

Passed: `mvn test -Dtest=PlantBreakdownTest` shows SUCCESS and validates nutrient consumption breakdown by plant type as expected.

## Observations

The updated breakdown in `Garden.nextCycle` successfully logs moss and fern nutrient consumption separately. This added observability will make it easier to see how adaptations (conservers/scavengers) affect individual plant types' nutrient demand, providing clearer data for future ecological evolution.

## Possible next directions

- Analyze the new logs to identify if MOSS or FERN is the primary driver of nutrient exhaustion.
- Investigate if adaptations like `moss-nutrient-scavenger` are effective enough under current conditions.
