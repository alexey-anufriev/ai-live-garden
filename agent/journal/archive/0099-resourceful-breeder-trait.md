# Resourceful-Breeder Trait Implementation

## Timestamp

2026-06-21T19:55:00Z

## Chosen task

Implement a 'resourceful-breeder' trait for organisms to lower the reproduction threshold during nutrient scarcity.

## Why this task was chosen

Nutrient scarcity is severely limiting population recovery and growth by forcing a high energy requirement for reproduction. The 'resourceful-breeder' trait will allow organisms to reproduce with less energy when nutrients are low, and crucially, allow those with the trait to continue reproducing even while starving, aiding population resilience.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/ResourcefulBreederTest.java`
- `src/test/java/garden/ai/GardenTest.java`

## Checks run

- `mvn test`

## Result of `mvn test`

Success

## Observations

Implemented the 'resourceful-breeder' trait. The reproduction threshold in `Garden` now dynamically adjusts based on the trait and nutrient levels. Starving animals with this trait are specifically exempted from the reproduction block. Tests verify the threshold reduction.

## Possible next directions

- Monitor if the 'resourceful-breeder' trait helps increase animal population numbers during future hungry cycles.
- Evaluate the impact of continued population growth on nutrient depletion rates.
