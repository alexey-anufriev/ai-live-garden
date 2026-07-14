package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

public class RootContributionEnhancementTest {

    @Test
    public void testRootContributionWithHighNutrients() {
        // Nutrients = 50 (>= 25 branch)
        // Traits: root-soil-enricher, root-nutrient-utilizer, root-nutrient-amplifier
        // Weights: 
        //   rootNetwork: max(1, 1/2) = 1
        //   rootSoilEnricherCount: 15 + bufferBonus (0) = 15
        //   rootNutrientUtilizerCount: 25
        //   rootNutrientAmplifierCount: 30
        // Total = 1 + 15 + 25 + 30 = 71
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "root-soil-enricher", "root-nutrient-utilizer", "root-nutrient-amplifier");
        // Environment: (50, 50, 50, 50, 50) - Nutrients >= 25
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(root1), List.of());
        
        assertEquals(71, garden.rootContribution());
    }

    @Test
    public void testRootContributionWithPumpAndDistributor() {
        // Nutrients = 50 (>= 25 branch)
        // Traits: nutrient-pump, nutrient-distributor
        // Weights: 
        //   rootNetwork: max(1, 1/2) = 1
        //   nutrientPumpCount: 10 (new value)
        //   nutrientDistributorCount: 8 (new value)
        // Total = 1 + 10 + 8 = 19
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-pump", "nutrient-distributor");
        // Environment: (50, 50, 50, 50, 50) - Nutrients >= 25
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(root1), List.of());
        
        assertEquals(19, garden.rootContribution());
    }
}
