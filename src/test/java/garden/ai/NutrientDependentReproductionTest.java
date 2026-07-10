package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class NutrientDependentReproductionTest {

    @Test
    public void testNutrientDependentReproductionModifier() {
        // High nutrients (> 75) AND High buffer (> 75) -> -15 for functional roles, -12 for others
        Environment veryHighEnv = new Environment(100, 100, 100, 100, 100);
        // High nutrients (> 75) AND Low buffer (<= 75) -> -10
        Environment highEnv = new Environment(100, 100, 100, 80, 50);
        // Mid nutrients (> 50, <= 75) -> -4
        Environment midNutrientEnv = new Environment(100, 100, 100, 60, 100);
        // Low nutrients (<= 50) -> 0
        Environment lowNutrientEnv = new Environment(100, 100, 100, 10, 100);
        
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "nutrient-dependent-reproduction");
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "nutrient-dependent-reproduction");
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-dependent-reproduction");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "nutrient-dependent-reproduction");

        int veryHighModifierFox = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", veryHighEnv, 0, fox);
        assertEquals(-15, veryHighModifierFox, "Modifier should be -15 for FOX (functional role)");

        int veryHighModifierFungus = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", veryHighEnv, 0, fungus);
        assertEquals(-15, veryHighModifierFungus, "Modifier should be -15 for FUNGUS (functional role)");

        int veryHighModifierRoot = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", veryHighEnv, 0, root);
        assertEquals(-15, veryHighModifierRoot, "Modifier should be -15 for ROOT_NETWORK (functional role)");

        int veryHighModifierBeetle = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", veryHighEnv, 0, beetle);
        assertEquals(-12, veryHighModifierBeetle, "Modifier should be -12 for BEETLE (non-functional role)");

        int highModifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", highEnv, 0, fox);
        assertEquals(-10, highModifier, "Modifier should be -10 for high nutrients (> 75) and low buffer (<= 75)");

        int midModifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", midNutrientEnv, 0, fox);
        assertEquals(-4, midModifier, "Modifier should be -4 for mid nutrients (> 50, <= 75)");

        int lowModifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", lowNutrientEnv, 0, fox);
        assertEquals(0, lowModifier, "Modifier should be 0 for low nutrients");
    }
}
