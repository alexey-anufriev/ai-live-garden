# Herbivore Scavenger Update

## Timestamp

2026-06-20T08:50:00Z

## Chosen task

Add ROOT_NETWORK to the prey sets of BEETLE and HARE in the OrganismType enum.

## Why this task was chosen

The garden's animal population has gone extinct in recent cycles, likely due to competitive exclusion or inability to find food in the plant-heavy environment. Adding ROOT_NETWORK to the herbivore diet enables scavenging behavior, which should improve their long-term chances of survival.

## Files changed

- `src/main/java/garden/ai/OrganismType.java`
- `agent/state.md`
- `README.md`

## Checks run

`mvn test`

## Result of `mvn test`

BUILD SUCCESS

## Observations

The herbivores were extinct in the current snapshot, and the plant population was dominating the ecosystem.

## Possible next directions

- Monitor if animals re-emerge naturally via reproduction or if a re-introduction mechanism is needed.
