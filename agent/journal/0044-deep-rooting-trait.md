# 0044-deep-rooting-trait.md

- chosen task: Introduce 'deep-rooting' trait.
- reason for choosing it: To add ecological depth, allowing plants to adapt better to dry periods, enhancing long-term ecosystem resilience against stress.
- files changed: `src/main/java/garden/ai/Garden.java`
- checks run: `mvn test`
- result of `mvn test`: BUILD SUCCESS
- observations: Added 'deep-rooting' trait, which protects plants from 'stressed' state when moisture < 30. Added to `mutationTrait` list. Verified tests pass.
- possible next directions: Monitor the effectiveness of the 'deep-rooting' trait, observe if it changes the plant population dynamics during dry cycles, consider if animal counterparts or other environmental adaptations are needed.
