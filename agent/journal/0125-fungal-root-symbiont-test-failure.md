# Investigating Fungal-Root-Symbiont Test Failure

## Timestamp

2026-06-23T10:55:00Z

## Chosen task

Investigate and fix the test failure in `FungalContributionTest.testRootContributionWithFungalRootSymbiont`.

## Why this task was chosen

The newly added test for `fungal-root-symbiont` failed, indicating that the trait is not correctly contributing to the `rootContribution` as expected. This suggests a potential issue in the logic or trait integration.

## Files changed

- `src/test/java/garden/ai/FungalContributionTest.java`

## Checks run

`mvn test -Dtest=FungalContributionTest`

## Result of `mvn test`

FAIL: `testRootContributionWithFungalRootSymbiont` (Expected 50, but got 5).

## Observations

The `rootContribution` calculation for `nutrients < 5` seems to be calculating only the base contribution (5). This implies `rootNetworkCount` might be counted incorrectly or the traits are not being detected on the organism.

## Possible next directions

- Debug `rootContribution` method in `Garden.java`.
- Inspect organism creation in the test.
