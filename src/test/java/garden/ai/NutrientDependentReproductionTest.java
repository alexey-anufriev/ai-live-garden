package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class NutrientDependentReproductionTest {

    @Test
    public void testNutrientDependentReproductionModifier() {
        Environment highNutrientEnv = new Environment(100, 100, 100, 100, 100);
        Environment lowNutrientEnv = new Environment(100, 100, 100, 10, 100);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "nutrient-dependent-reproduction");

        // High nutrients (> 50), modifier should be -4
        int highModifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", highNutrientEnv, 0, fox);
        assertEquals(-4, highModifier, "Modifier should be -4 for high nutrients");

        // Low nutrients (<= 50), modifier should be 0
        int lowModifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", lowNutrientEnv, 0, fox);
        assertEquals(0, lowModifier, "Modifier should be 0 for low nutrients");
    }
}
