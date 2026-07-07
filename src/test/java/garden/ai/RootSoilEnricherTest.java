package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RootSoilEnricherTest {
    @Test
    public void testRootSoilEnricherGrowthBonus() {
        Environment envHigh = new Environment(60, 60, 60, 60, 100); // High buffer
        Environment envLow = new Environment(60, 60, 60, 60, 10);  // Low buffer
        Organism root = new Organism("root-1", OrganismType.ROOT_NETWORK, 10, 5, 1, List.of("root-soil-enricher"));
        
        TraitRegistry.PlantGrowthEffect effectHigh = TraitRegistry.getPlantGrowthEffect("root-soil-enricher", 1, root, envHigh, List.of(root), 0);
        TraitRegistry.PlantGrowthEffect effectLow = TraitRegistry.getPlantGrowthEffect("root-soil-enricher", 1, root, envLow, List.of(root), 0);
        
        assertTrue(effectHigh != null && effectHigh.growthChange() > 0, "Root-soil-enricher should provide a growth bonus when buffer is high.");
        assertTrue(effectLow == null, "Root-soil-enricher should not provide a growth bonus when buffer is low.");
    }
}
