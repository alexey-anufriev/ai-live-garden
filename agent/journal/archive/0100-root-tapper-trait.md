# Journal Entry: Root-Tapper Trait Implementation

## Timestamp

2026-06-21T20:50:45Z

## Chosen task

Implement a 'root-tapper' trait for animals to gain an energy bonus when near root networks.

## Why this task was chosen

Animals interact too little with the robust root networks that form the foundation of the garden's nutrient cycle. The 'root-tapper' trait creates a new symbiotic link, allowing animals to actively benefit from these networks, thereby increasing ecosystem interdependence and animal resilience.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/RootTapperTest.java`

## Checks run

- `mvn test`

## Result of `mvn test`

Success

## Observations

Implemented the 'root-tapper' trait in `Garden.feedingPhase`. Animals with this trait now gain +1 energy when feeding if any `ROOT_NETWORK` organisms are present in the garden, representing them tapping into plant-managed nutrient resources.

## Possible next directions

- Monitor if the 'root-tapper' trait improves survival rates for animals during hungry cycles.
- Observe if the trait leads to any unexpected population shifts.
