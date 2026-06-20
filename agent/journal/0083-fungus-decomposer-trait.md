# Journal Entry: 0083-fungus-decomposer-trait

## Reasoning
The garden is suffering from critical nutrient scarcity. The recently introduced `FUNGUS` organisms are intended to act as decomposers, but currently, they only have a static nutrient contribution logic. I am introducing a 'nutrient-decomposer' trait that increases the nutrient value of organisms upon death, and I'm adding this trait to the FUNGUS mutation pool so that it can evolve to fulfill its decomposer role more effectively.

## Implementation
- Added 'nutrient-decomposer' trait logic to `Organism.nutrientValue()`.
- Added 'nutrient-decomposer' to the mutation pool in `Garden.mutationTrait()`.

## Observations
- This trait increases nutrient return significantly (value +7) to promote better nutrient cycling when organisms die.
- Verified with `mvn test`.

## Next Steps
- Observe if FUNGUS organisms evolve this trait in future cycles.
- Monitor nutrient levels to see if this improves availability in the long term.