# Mycelial-Resonator Trait Implementation

## Timestamp

2026-06-22T11:47:00Z

## Chosen task

Introduce a 'mycelial-resonator' trait for animals to gain a passive metabolic reduction when in proximity to fungal networks.

## Why this task was chosen

With herbivores now able to actively scavenge from and harvest fungal networks, 'mycelial-resonator' adds a passive layer of symbiosis, further strengthening the interdependence between animals and the fungal-root network.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/test/java/garden/ai/MycelialResonatorTest.java`

## Checks run

- `mvn test`

## Result of `mvn test`

All tests passed.

## Observations

The 'mycelial-resonator' trait provides a passive metabolic benefit for animals when a fungal network is present (fungalContribution > 0), complementing the active 'mycelial-scavenger' trait.

## Possible next directions

- Observe if 'mycelial-resonator' helps stabilize animal populations in habitats with strong fungal presence.
- Continue exploring passive interdependencies within the garden.
