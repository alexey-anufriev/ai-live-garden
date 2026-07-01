# Extract Contribution Calculation Logic

## Timestamp

2026-07-01T12:53:43Z

## Chosen task

Consolidate root, fungal, and other contribution calculations into a new ContributionCalculator class and refactor Garden.nextCycle to use it.

## Why this task was chosen

This refactoring modularizes the simulation loop further, reducing the complexity of Garden.nextCycle and consolidating duplicate contribution logic.

## Files changed

- `README.md`
- `agent/code-map.md`
- `agent/garden-trends.svg`
- `agent/journal/0210-extract-contribution-calculation-logic.md`
- `agent/organism-trends.svg`
- `agent/state.md`
- `agent/summaries/daily/2026-07-01.md`
- `data/garden-state.txt`
- `src/main/java/garden/ai/ContributionCalculator.java`
- `src/main/java/garden/ai/Garden.java`
- `src/main/java/garden/ai/PassiveChangeCalculator.java`

## Checks run

mvn -B test

## Result of `mvn test`

Passed.

## Observations

Refactoring simulation phases into focused calculator classes continues to improve the modularity and maintainability of the Garden class. Expected future effect: Improved maintainability and readability of the simulation loop, with no functional changes to garden behavior. Automated post-processing refreshed README/state memory from data/garden-state.txt at cycle 5566.

## Possible next directions

- Continue simplifying the main Garden.nextCycle loop, perhaps by examining the FeedingPhase or ReproductionPhase orchestration further.
