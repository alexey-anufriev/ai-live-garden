# Nutrient-Sharer Trait Implementation

## Timestamp

2026-06-21T12:00:00Z

## Chosen task

Implement the `nutrient-sharer` trait for animals to mitigate starvation-driven mortality.

## Why this task was chosen

Animals in the garden are suffering from critical nutrient scarcity and frequent starvation-driven mortality. The `nutrient-sharer` trait will reduce metabolic expenditure when starving and increase nutrient return upon death, providing a survival buffer and balancing the ecosystem.

## Files changed

- `src/main/java/garden/ai/Organism.java`
- `src/test/java/garden/ai/NutrientSharerTraitTest.java`

## Checks run

Unit tests.

## Result of `mvn test`

All tests pass.

## Observations

Starvation is the primary driver of mortality during nutrient depletion. The new trait provides a slight survival advantage for starving animals, and they become more valuable nutrient sources upon death.

## Possible next directions

- Monitor animal population resilience during lean cycles.
