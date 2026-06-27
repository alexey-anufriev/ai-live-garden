package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NutrientDistributorTest {
    @Test
    public void testNutrientDistributorEnhancesBufferRelease() {
        Environment env = new Environment(1, 100, 100, 1, 100);
        List<Organism> organisms = new ArrayList<>();
        // Add a distributor organism
        organisms.add(Organism.of("distributor-1", OrganismType.ROOT_NETWORK, 10, 10, "nutrient-distributor"));
        
        Garden garden = new Garden(1, 10, env, organisms, new ArrayList<>());
        
        // Advance cycle
        Garden nextGarden = garden.nextCycle();
        
        // Check events to see if nutrient distributor was used
        boolean distributorUsed = nextGarden.events().stream()
                .anyMatch(e -> e.description().contains("distributors=1"));
        
        assertTrue(distributorUsed, "Nutrient distributor should be reflected in buffer release stats.");
    }
}
