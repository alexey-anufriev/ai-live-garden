# Sun Lover Trait

## Timestamp

2026-06-18T00:49:12Z

## Chosen task

Introduce the 'sun-lover' trait for plants.

## Why this task was chosen

The garden's ecosystem needs more ecological depth to survive nutrient stress. Plants with the 'sun-lover' trait grow faster when light conditions are strong, rewarding organisms that occupy high-light niches. This adds a new behavioral dimension to the plant population without adding excessive complexity.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`

## Checks run

`mvn test`

## Result of `mvn test`

BUILD SUCCESS.

## Observations

The new trait is integrated into the mutation mechanism and passive plant growth logic. Tests confirm that a plant with the trait in high-light conditions receives an extra energy boost. This should help plants thrive in high-light scenarios, providing a small counter-balance to the nutrient-depleted state.

## Possible next directions

- Monitor if the trait becomes dominant.
- Consider adding traits for animals that affect their foraging efficiency.
- Deepen the relationship between environmental conditions and organism behavior further.
