# 0103-nutrient-reclaimer-trait

## Timestamp

2026-06-22T08:50:00Z

## Chosen task

Introduce a 'nutrient-reclaimer' trait for predators to recover extra nutrients from prey with the 'nutrient-storer' trait.

## Why this task was chosen

With the recent introduction of the 'nutrient-storer' trait for animals, prey have become living nutrient banks. The 'nutrient-reclaimer' trait completes this feedback loop by allowing predators to actively recover these banked nutrients, strengthening the food web and nutrient cycle in the face of persistent nutrient scarcity.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/NutrientReclaimerTest.java`

## Checks run

- `mvn test`

## Result of `mvn test`

Success

## Observations

Predators with the 'nutrient-reclaimer' trait now gain a significant energy bonus when consuming prey with the 'nutrient-storer' trait, creating a new, focused nutrient recycling pathway through predation.

## Possible next directions

- Observe if this trait leads to predator population booms during nutrient scarcity.
- Evaluate the impact on the overall nutrient buffer stability.
