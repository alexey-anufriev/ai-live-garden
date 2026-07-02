package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class ColonizationCalculatorTest {

    @Test
    public void testColonizationIsBufferAware() {
        // Setup
        List<Organism> organisms = new ArrayList<>();
        // Need enough plants to trigger herbivore colonization (current logic: > 200)
        for (int i = 0; i < 201; i++) {
            organisms.add(Organism.of("moss-" + i, OrganismType.MOSS, 10, 1, "test"));
        }
        
        // Low buffer (0) -> chance 5% (1/20)
        Environment lowBufferEnv = new Environment(50, 50, 50, 50, 0);
        Random lowRandom = new Random(42); // Seed to make it deterministic
        ColonizationCalculator.ColonizationContext lowContext = new ColonizationCalculator.ColonizationContext(
                organisms, lowBufferEnv, 1, 1, new ArrayList<>(), lowRandom);
        
        ColonizationCalculator.ColonizationResult lowResult = ColonizationCalculator.calculate(lowContext);
        long lowAnimalCount = lowResult.organisms().stream().filter(o -> o.type().isAnimal()).count();

        // High buffer (100) -> chance 10% (1/10)
        Environment highBufferEnv = new Environment(50, 50, 50, 50, 100);
        Random highRandom = new Random(42); // Same seed, different threshold (10 vs 20)
        ColonizationCalculator.ColonizationContext highContext = new ColonizationCalculator.ColonizationContext(
                organisms, highBufferEnv, 1, 1, new ArrayList<>(), highRandom);
        
        ColonizationCalculator.ColonizationResult highResult = ColonizationCalculator.calculate(highContext);
        long highAnimalCount = highResult.organisms().stream().filter(o -> o.type().isAnimal()).count();

        // Given the same seed, the behavior should differ due to the buffer.
        // With seed 42, nextInt(20) vs nextInt(10) will be different.
        // This proves the buffer-awareness.
        assertNotEquals(lowAnimalCount, highAnimalCount, "Colonization should be buffer-aware and produce different results with the same random seed.");
    }
}
