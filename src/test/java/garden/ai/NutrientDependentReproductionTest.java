package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class NutrientDependentReproductionTest {

    @Test
    public void testNutrientDependentReproductionModifier() {
        // High nutrients (> 75) AND High buffer (> 75) -> -12
        Environment veryHighEnv = new Environment(100, 100, 100, 100, 100);
        // High nutrients (> 75) AND Low buffer (<= 75) -> -10
        Environment highEnv = new Environment(100, 100, 100, 80, 50);
        // Mid nutrients (> 50, <= 75) -> -4
        Environment midNutrientEnv = new Environment(100, 100, 100, 60, 100);
        // Low nutrients (<= 50) -> 0
        Environment lowNutrientEnv = new Environment(100, 100, 100, 10, 100);
        
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "nutrient-dependent-reproduction");

        int veryHighModifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", veryHighEnv, 0, fox);
        assertEquals(-12, veryHighModifier, "Modifier should be -12 for high nutrients (> 75) and high buffer (> 75)");

        int highModifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", highEnv, 0, fox);
        assertEquals(-10, highModifier, "Modifier should be -10 for high nutrients (> 75) and low buffer (<= 75)");

        int midModifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", midNutrientEnv, 0, fox);
        assertEquals(-4, midModifier, "Modifier should be -4 for mid nutrients (> 50, <= 75)");

        int lowModifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", lowNutrientEnv, 0, fox);
        assertEquals(0, lowModifier, "Modifier should be 0 for low nutrients");
    }
}
