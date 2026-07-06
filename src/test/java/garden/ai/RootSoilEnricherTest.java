package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

public class RootSoilEnricherTest {

    @Test
    public void testRootContributionWithRootSoilEnricher() {
        // Nutrients = 50 (>= 25)
        // Traits: root-soil-enricher
        // Base ROOT_NETWORK: Math.max(1, 1/2) = 1
        // RootSoilEnricher: 1 * 7 = 7
        // Total = 1 + 7 = 8
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "root-soil-enricher");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(root1), List.of());
        
        assertEquals(8, garden.rootContribution());
    }
}
