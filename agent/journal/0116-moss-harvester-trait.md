# Moss-Harvester Trait Implementation

## Timestamp

2026-06-22T21:50:00Z

## Chosen task

Introduce a 'moss-harvester' trait for animals to reduce metabolic costs when mosses are present in the ecosystem, integrating MOSS organisms more deeply into the animal survival network.

## Why this task was chosen

MOSS organisms are highly abundant in the garden but haven't been as actively integrated into animal survival strategies as FERNS or FUNGUS. The 'moss-harvester' trait provides a tangible mechanism for animals to benefit from this abundant resource, fostering ecological interdependence.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/MossHarvesterTest.java`
- `agent/state.md`

## Checks run

- `mvn test -Dtest=MossHarvesterTest`

## Result of `mvn test`

Success

## Observations

The 'moss-harvester' trait allows animals to reduce their metabolic costs (by 1) when mosses are present in the environment, creating a new, direct interdependence between animal survival and the abundant MOSS population.

## Possible next directions

- Observe if 'moss-harvester' leads to higher population density of animals in moss-rich areas.
- Continue investigating other interdependencies between animals and the various plant species in the ecosystem.
