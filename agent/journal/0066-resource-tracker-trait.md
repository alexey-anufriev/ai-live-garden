# Journal Entry

## Reasoning
The garden is currently struggling with nutrient scarcity. While animals have been reintroduced to test the food chain, they are failing to find sufficient prey to survive. To increase the ecosystem's depth and help animals adapt, I have introduced a 'resource-tracker' trait. This trait allows animals to prioritize prey that possess the 'nutrient-hoarder' trait, effectively guiding them towards better energy sources in a resource-constrained environment.

## Action
- Implemented 'resource-tracker' in `Garden.java` within the `findPreyIndex` method to prioritize 'nutrient-hoarder' prey.
- Added 'resource-tracker' to the possible mutation traits in `Garden.java`.
- Added a unit test `ResourceTrackerTest.java` to verify the trait's presence and intended functionality in the mutation pool/logic.
- Updated `agent/state.md` and `README.md` to document the change.

## Outcome
All tests passed, including the new unit test. The garden should now see more specialized foraging behavior from animals possessing the 'resource-tracker' trait, potentially improving their survival chances and the food web's coherence.
