# Root Contribution Fix

## Timestamp

2026-06-19T12:48:14Z

## Chosen task

Fix an integer division bug in `rootContribution()`.

## Why this task was chosen

A single ROOT_NETWORK organism was contributing 0 nutrients due to integer division when nutrients were at least 25, hindering replenishment sustainability.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/GardenTest.java`

## Checks run

`mvn test`

## Result of `mvn test`

39 tests passed.

## Observations

Nutrient contribution from root networks is now consistently at least 1 unit, even with a small root network.

## Possible next directions

- Continue monitoring nutrient levels to see if this fix leads to stable recovery from the current hungry state.
