# Journal Entry 0040 - nutrient-hoarder-trait

- chosen task: Introduce 'nutrient-hoarder' trait for plants.
- reason for choosing it: The garden is experiencing periods of nutrient scarcity. This trait creates a long-term benefit for ecosystem recovery by allowing plants that survive in hungry conditions to contribute extra nutrients when they die, creating a positive feedback loop for nutrient accumulation.
- files changed: src/main/java/garden/ai/Organism.java, src/main/java/garden/ai/Garden.java
- checks run: mvn test
- result of mvn test: BUILD SUCCESS
- observations: Plants can now evolve the 'nutrient-hoarder' trait. Upon death, they contribute more nutrients (type value + 5) to the environment. This should make the 'hungry' periods less catastrophic as the ecosystem builds up nutrient reserves through plant life cycles.
- possible next directions: Monitor the impact of this trait on long-term nutrient levels; maybe add a trait that allows animals to specifically target 'nutrient-hoarder' plants.
