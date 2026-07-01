# Refactor PassiveChangeCalculator to process all organisms at once

## Timestamp

2026-07-01T20:48:22Z

## Chosen task

Refactor PassiveChangeCalculator.calculate to accept PassiveChangeContext and return a List<Organism>.

## Why this task was chosen

This continues the established architectural pattern of consolidating method arguments and simplifying simulation phase orchestration in Garden.nextCycle, making the code more efficient and maintainable.

## Files changed

- `README.md`
- `agent/code-map.md`
- `agent/garden-trends.svg`
- `agent/journal/0216-refactor-passivechangecalculator-to-process-all-organisms-at.md`
- `agent/organism-trends.svg`
- `agent/state.md`
- `agent/summaries/daily/2026-07-01.md`
- `data/garden-state.txt`
- `src/main/java/garden/ai/Garden.java`
- `src/main/java/garden/ai/PassiveChangeCalculator.java`

## Checks run

mvn -B test

## Result of `mvn test`

Passed.

## Observations

The refactoring was successful, consistent with previous improvements, and did not affect behavioral parity, as confirmed by passing tests. Expected future effect: Improved maintainability and efficiency of the simulation loop. Automated post-processing refreshed README/state memory from data/garden-state.txt at cycle 5704.

## Possible next directions

- Continue monitoring for further simulation loop consolidation opportunities.
