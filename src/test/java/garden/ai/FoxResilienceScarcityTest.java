package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxResilienceScarcityTest {

    @Test
    public void testFoxMetabolicEfficiencyWithScarcity() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "fox-metabolic-efficiency");
        Environment environment = new Environment(1, 10, 10, 10, 10);
        
        // Scenario 1: Low beetle count (should trigger resilience bonus)
        long lowBeetleCount = 5;
        // Scenario 1.5: Critical beetle count (should also trigger resilience bonus)
        long criticalBeetleCount = 12;
        
        TraitRegistry.MetabolicEffect lowBeetleResult = TraitRegistry.calculateMetabolism(1, fox, environment, 0, 0, 0, lowBeetleCount);
        TraitRegistry.MetabolicEffect criticalBeetleResult = TraitRegistry.calculateMetabolism(1, fox, environment, 0, 0, 0, criticalBeetleCount);
        
        // Scenario 2: High beetle count (should NOT trigger resilience bonus)
        long highBeetleCount = 500;
        TraitRegistry.MetabolicEffect highBeetleResult = TraitRegistry.calculateMetabolism(1, fox, environment, 0, 0, 0, highBeetleCount);

        assertTrue(lowBeetleResult.energyBonus() > highBeetleResult.energyBonus(), "Fox should have higher energy bonus when beetle population is low.");
        assertEquals(8, lowBeetleResult.energyBonus(), "Expected higher energy bonus for fox-metabolic-efficiency with low beetles.");
        assertEquals(8, criticalBeetleResult.energyBonus(), "Expected higher energy bonus for fox-metabolic-efficiency with critical beetles.");
        assertEquals(4, highBeetleResult.energyBonus(), "Expected base energy bonus for fox-metabolic-efficiency with high beetles.");
    }
}
