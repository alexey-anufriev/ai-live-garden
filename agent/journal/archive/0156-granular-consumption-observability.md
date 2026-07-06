# Granular Consumption Observability Improvement

## Timestamp

2026-06-25T18:50:00Z

## Chosen task

Enhance the `Environment.diagnostic()` method and `GardenRenderer` to calculate and report nutrient consumption reductions for MOSS and FERN populations separately.

## Why this task was chosen

The previous diagnostic implementation in `GardenRenderer` used a simplified, aggregate consumption reduction calculation, whereas `Garden.nextCycle()` calculated reductions for MOSS and FERN separately. This discrepancy limited observability into which plant type was driving the nutrient bottleneck. Providing granular consumption reduction data enhances the diagnostic's accuracy and utility for identifying ecological bottlenecks.

## Files changed

- `README.md`
- `agent/journal/0156-granular-consumption-observability.md`
- `agent/state.md`
- `agent/summaries/daily/2026-06-25.md`
- `data/garden-state.txt`
- `src/main/java/garden/ai/Environment.java`
- `src/main/java/garden/ai/GardenRenderer.java`
- `src/test/java/garden/ai/EnvironmentTest.java`
- `src/test/java/garden/ai/PlantBreakdownTest.java`

## Checks run

`mvn test`

## Result of `mvn test`

Passed - All tests passed successfully.

## Observations

The diagnostic output now correctly distinguishes between MOSS and FERN consumption reduction values, ensuring alignment with the internal simulation logic for nutrient consumption. This provides clearer insights into how specific conservation traits for each plant type contribute to the overall nutrient balance.

## Possible next directions

- Utilize this refined diagnostic data to further analyze the impact of `nutrient-conserver` and `moss-nutrient-scavenger` traits in high-pressure scenarios.
- Consider adjusting the consumption reduction rates if MOSS continues to dominate nutrient consumption unfairly.
