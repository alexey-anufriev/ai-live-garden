# Orchestrate simulation in OrganismInteractionCalculator

## Timestamp

2026-07-03T20:50:26Z

## Chosen task

Moved orchestration logic from Garden.nextCycle() to OrganismInteractionCalculator.advance(Garden).

## Why this task was chosen

This separates simulation rules/orchestration from the Garden data object, improving domain boundaries and maintainability.

## Files changed

- `README.md`
- `agent/code-map.md`
- `agent/garden-trends.svg`
- `agent/journal/0235-orchestrate-simulation-in-organisminteractioncalculator.md`
- `agent/organism-trends.svg`
- `agent/state.md`
- `agent/summaries/daily/2026-07-03.md`
- `data/garden-state.txt`
- `src/main/java/garden/ai/Garden.java`
- `src/main/java/garden/ai/OrganismInteractionCalculator.java`

## Checks run

mvn -B test

## Result of `mvn test`

Passed.

## Observations

Successfully refactored the orchestration logic without functional changes or regressions, as confirmed by all tests passing. Expected future effect: Improved architectural clarity and maintainability for future simulation evolution. Automated post-processing refreshed README/state memory from data/garden-state.txt at cycle 6442.

## Possible next directions

- Continue exploring further consolidation of simulation rules.
