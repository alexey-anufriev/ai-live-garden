# Agent State

## Purpose Of This File

This file is the compact current operating memory for future work on the garden. It should explain what the garden is now, what the current snapshot shows, which simulation rules matter most, and the primary direction for the next few runs.

## Current Garden State

- Cycle: 1991 (stable advancement).
- Nutrients: 0 (persistently scarce).
- NutrientBuffer: 100 (primary survival resource).
- Key Ecological Drivers:
  - FUNGUS networks actively recycle and enrich the soil.
  - ROOT_NETWORKs facilitate interdependencies (buffer optimization, nutrient sharing).
  - Animals rely on diverse traits for survival under scarcity (scavenging, hoarding, buffering).
  - Newly added 'mycelial-scavenger' trait allows herbivores to leverage fungal contribution.
  - Newly added 'mycelial-harvester' trait enhances the metabolism reduction of fungal scavenging.
  - Newly added 'nutrient-reclaimer' trait allows predators to reclaim banked nutrients from prey.
  - Newly added 'mycelial-distributor' trait allows animals to boost the nutrient buffer upon death when near fungal networks.

## Immediate Directions

- Continue observing how animals utilize the nutrient buffer and fungal contributions.
- Focus on traits that integrate animal survival into the established fungal-root network.
- Maintain stability; avoid excessive complexity.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add more standalone traits unless they create a visible relationship or address a current pressure.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
