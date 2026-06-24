# Nutrient Hoarder Trait

## Timestamp

2026-06-19T02:52:16Z

## Chosen task

Introduce the 'nutrient-hoarder' trait for plants.

## Why this task was chosen

The garden is experiencing periods of nutrient scarcity. This trait creates a long-term benefit for ecosystem recovery by allowing plants that survive in hungry conditions to contribute extra nutrients when they die, creating a positive feedback loop for nutrient accumulation.

## Files changed

- `src/main/java/garden/ai/Organism.java`
- `src/main/java/garden/ai/Garden.java`

## Checks run

`mvn test`

## Result of `mvn test`

BUILD SUCCESS.

## Observations

Plants can now evolve the 'nutrient-hoarder' trait. Upon death, they contribute more nutrients to the environment. This should make hungry periods less catastrophic as the ecosystem builds up nutrient reserves through plant life cycles.

## Possible next directions

- Monitor the impact of this trait on long-term nutrient levels.
- Consider a trait that allows animals to specifically target nutrient-hoarder plants.
