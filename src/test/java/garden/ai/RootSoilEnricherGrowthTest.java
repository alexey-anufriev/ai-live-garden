package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;

public class RootSoilEnricherGrowthTest {

    @Test
    public void testRootSoilEnricherGrowthEffect() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "root-soil-enricher");
        Environment envWithHighBuffer = new Environment(50, 50, 50, 50, 51); // Buffer > 50
        Environment envWithLowBuffer = new Environment(50, 50, 50, 50, 50); // Buffer <= 50

        // Test with high buffer
        TraitRegistry.PlantGrowthEffect effectHigh = TraitRegistry.getPlantGrowthEffect("root-soil-enricher", 1, root, envWithHighBuffer, List.of(), 0);
        assertNotNull(effectHigh);
        assertEquals(2, effectHigh.growthChange());

        // Test with low buffer
        TraitRegistry.PlantGrowthEffect effectLow = TraitRegistry.getPlantGrowthEffect("root-soil-enricher", 1, root, envWithLowBuffer, List.of(), 0);
        assertEquals(null, effectLow);
    }
}
