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
  - Newly added 'mycelial-distributor' trait allows animals to contribute nutrients back to the buffer upon death when near fungal networks.
  - Newly added 'mycelial-resonator' trait allows animals to gain passive metabolic reduction when in proximity to fungal networks.
  - Newly added 'buffer-explorer' trait allows animals to reduce metabolism and gain additional energy when tapping the nutrient buffer.
  - Newly added 'mycelial-network-scout' trait allows animals to use fungal networks to scout for prey.
  - Newly added 'fungal-gardener' trait allows animals to actively improve the contribution of fungal networks when they are nearby.
  - Newly added 'spore-disperser' trait allows animals to reduce metabolic costs by dispersing spores when interacting with fungal networks.
  - Newly added 'fungal-fertilizer' trait allows animals to actively increase fungal contributions when in proximity.
  - Newly added 'nutrient-anticipator' trait allows animals to pre-emptively reduce metabolic costs when the nutrient buffer is low.
  - Newly added 'fungal-nurturer' trait allows animals to improve reproduction success when in proximity to fungal networks.
  - Newly added 'mycelial-protector' trait allows animals to gain stronger metabolic reduction when in proximity to fungal networks.
  - Newly added 'metabolic-economizer' trait allows animals to reduce metabolic costs when stressed.
  - Newly added 'nutrient-refiner' trait allows animals to gain an extra energy boost when feeding, provided they are not stressed.
  - Newly added 'fungal-root-symbiont' trait allows root networks to more efficiently leverage fungal network activity, enhancing nutrient recycling.
  - Newly added 'mycelial-root-mediator' trait allows animals to enhance root network nutrient contribution when in proximity to fungal networks, completing a tripartite feedback loop between animals, fungi, and root networks.
  - Newly added 'fungal-attractor' trait allows ROOT_NETWORK organisms to attract animals when in proximity to fungal networks, providing a localized boost to animal energy-gathering efficiency.


## Immediate Directions

- Continue observing how animals utilize the nutrient buffer and fungal contributions.
- Focus on traits that integrate animal survival into the established fungal-root network.
- Maintain stability; avoid excessive complexity.

## Constraints & Known Bad Ideas

- Do not attempt to fix the simulation in one run.
- Do not add more standalone traits unless they create a visible relationship or address a current pressure.
- Do not treat the full nutrient buffer as proof of health while nutrients are zero and animals are absent.
