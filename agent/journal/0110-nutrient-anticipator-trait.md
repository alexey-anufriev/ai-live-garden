# Nutrient-Anticipator Trait Implementation

## Timestamp

2026-06-22T15:50:00Z

## Chosen task

Introduce a 'nutrient-anticipator' trait for animals to reduce metabolism when nutrient buffer is low.

## Why this task was chosen

Animals are still struggling with nutrient scarcity. The 'nutrient-anticipator' trait adds a predictive, resilience-based mechanism, allowing them to lower their metabolic costs *before* they enter a critical state, when the nutrient buffer is already low.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/NutrientAnticipatorTest.java`
- `agent/state.md`

## Checks run

- `mvn test -Dtest=NutrientAnticipatorTest`

## Result of `mvn test`

Success

## Observations

The 'nutrient-anticipator' trait allows animals to pre-emptively reduce metabolism (by 1) when the nutrient buffer is below 20, fostering better survival prospects in anticipation of scarcity.

## Possible next directions

- Observe if 'nutrient-anticipator' contributes to higher long-term animal survival rates during periods of low nutrient buffer.
- Continue investigating other adaptive survival mechanisms.
