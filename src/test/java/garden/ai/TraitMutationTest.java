package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TraitMutationTest {
    @Test
    void testBiasedMutationForLowNutrients() {
        Environment lowNutrientsEnvironment = new Environment(1, 100, 100, 5, 100); // nutrients=5
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "test");
        
        int metabolicEfficiencyCount = 0;
        int iterations = 2000;
        
        for (int i = 0; i < iterations; i++) {
            String trait = TraitRegistry.getMutationTrait(i, beetle, beetle.type(), lowNutrientsEnvironment);
            if (trait.equals("metabolic-efficiency")) metabolicEfficiencyCount++;
        }
        
        // Plant/Animal check: BEETLE is animal -> should get metabolic-efficiency
        // 20% bias -> expect around 400. Even with deterministic fallback, should be > 10%
        assertTrue(metabolicEfficiencyCount > iterations * 0.1, "Metabolic efficiency should be frequent: " + metabolicEfficiencyCount);
    }
}
