# Quiet-Hunger Trait

## Timestamp

2026-06-19T17:50:00Z

## Chosen task

Implement 'quiet-hunger' trait for animals to reduce metabolic cost when starving.

## Why this task was chosen

To improve resilience in the hungry ecosystem, specifically addressing starvation-induced population crashes.

## How it was done

- Modified `passiveChange` in `Garden.java` to apply a metabolic reduction when an animal has both 'quiet-hunger' and 'starving' traits.
- Added a new test case in `GardenTest.java` to verify the metabolic reduction.
- Confirmed with `mvn test`.

## Possible next directions

- Observe if this trait effectively reduces the frequency of predator/prey starvation-induced population crashes.
