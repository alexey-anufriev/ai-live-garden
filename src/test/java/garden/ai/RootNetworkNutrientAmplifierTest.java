package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RootNetworkNutrientAmplifierTest {
    @Test
    void testRootNetworkNutrientAmplifierContribution() {
        Environment environment = new Environment(1, 100, 100, 100, 100); // nutrients=100
        Organism root1 = Organism.of("root1", OrganismType.ROOT_NETWORK, 10, 1, "none");
        Organism root2 = Organism.of("root2", OrganismType.ROOT_NETWORK, 10, 1, "root-nutrient-amplifier");
        
        List<Organism> organismsWithoutTrait = List.of(root1);
        List<Organism> organismsWithTrait = List.of(root2);
        
        int contributionWithoutTrait = TraitRegistry.calculateRootContribution(organismsWithoutTrait, environment, 0);
        int contributionWithTrait = TraitRegistry.calculateRootContribution(organismsWithTrait, environment, 0);
        
        assertTrue(contributionWithTrait > contributionWithoutTrait, 
            "Contribution with trait should be higher: " + contributionWithTrait + " > " + contributionWithoutTrait);
        assertEquals(20, contributionWithTrait - contributionWithoutTrait, 
            "Difference should be exactly 20");
    }
}
