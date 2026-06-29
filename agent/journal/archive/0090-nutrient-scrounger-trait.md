# Nutrient-Scrounger Trait Implementation

## Timestamp

2026-06-21T10:50:00Z

## Chosen task

Implement a 'nutrient-scrounger' trait for ANIMALS (HERBIVORE, PREDATOR) to improve their survival in nutrient-scarce environments.

## Why this task was chosen

The current garden state is critically hungry, with nutrient levels near zero. While plants have various mechanisms to cope, animals are struggling to maintain energy. This trait provides a mechanism for animals to gain extra energy in low-nutrient conditions, potentially helping them persist while the ecosystem attempts to restore nutrient balance. I also had to fix an `UnsupportedOperationException` in `findPreyIndex` caused by incorrect usage of the immutable record field `events` instead of the mutable parameter.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`

## Checks run

`mvn test`

## Result of `mvn test`

Success

## Observations

Implemented nutrient-scrounger trait for animals, observed positive impact in simulated hungry conditions; fixed `UnsupportedOperationException` in `findPreyIndex`.

## Possible next directions

- Observe the proliferation and impact of the 'nutrient-scrounger' trait in subsequent simulation ticks.
- Evaluate if further animal-specific traits are needed to support a balanced food web.
- Monitor nutrient buffer levels to ensure the 'nutrient-scrounger' trait doesn't lead to rapid exhaustion of resources.
