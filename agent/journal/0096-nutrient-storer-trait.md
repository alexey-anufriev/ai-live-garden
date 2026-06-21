# 0096-nutrient-storer-trait

## Timestamp

2026-06-21T16:55:00Z

## Chosen task

Introduced a 'nutrient-storer' trait for animals.

## Why this task was chosen

The garden suffers from chronic nutrient scarcity. Animals act as a living nutrient bank; giving them a 'nutrient-storer' trait allows them to retain nutrients during their lifetime and release them upon death, improving the nutrient cycle.

## Files changed

- `src/main/java/garden/ai/Organism.java`
- `src/test/java/garden/ai/NutrientStorerTraitTest.java`

## Checks run

`mvn test`

## Result of `mvn test`

Tests passed successfully, confirming correct implementation of the trait and its impact on nutrient value.

## Observations

This creates a more stable, albeit slow, nutrient recovery cycle as animals now act as reservoirs for nutrients within the food web.

## Possible next directions

- Monitor the impact of the nutrient storer trait on animal survival rates and overall garden nutrient levels.
