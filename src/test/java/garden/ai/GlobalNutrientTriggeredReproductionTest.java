package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GlobalNutrientTriggeredReproductionTest {

    @Test
    public void testGlobalNutrientTriggeredReproductionModifier() {
        // High nutrients (> 75) AND High buffer (> 75) -> -5 for functional roles (FOX, FUNGUS, ROOT_NETWORK)
        Environment highEnv = new Environment(100, 100, 100, 80, 80);
        
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1);
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1);
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1);

        assertEquals(-5, TraitRegistry.getGlobalReproductionThresholdModifier(highEnv, fox), "Modifier should be -5 for FOX");
        assertEquals(-5, TraitRegistry.getGlobalReproductionThresholdModifier(highEnv, fungus), "Modifier should be -5 for FUNGUS");
        assertEquals(-5, TraitRegistry.getGlobalReproductionThresholdModifier(highEnv, root), "Modifier should be -5 for ROOT_NETWORK");
        assertEquals(0, TraitRegistry.getGlobalReproductionThresholdModifier(highEnv, beetle), "Modifier should be 0 for BEETLE");

        // Lower nutrients -> 0
        Environment lowEnv = new Environment(100, 100, 100, 50, 80);
        assertEquals(0, TraitRegistry.getGlobalReproductionThresholdModifier(lowEnv, fox), "Modifier should be 0 for low nutrients");
    }
}
