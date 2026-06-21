package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

public class FungalContributionTest {

    @Test
    public void testFungalContributionWithConnector() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-network-connector");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1), List.of());
        
        // Count: 1 FUNGUS (2), 1 connector (4) = 6
        assertEquals(6, garden.fungalContribution());
    }

    @Test
    public void testFungalContributionWithConnectorAndRootNetwork() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-network-connector");
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-weaver");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1, root1), List.of());
        
        // Count: 1 FUNGUS (2), 1 connector (6 bonus) = 8
        assertEquals(8, garden.fungalContribution());
    }

    @Test
    public void testFungalContributionWithSymbiote() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "nutrient-decomposer");
        Organism fern1 = Organism.of("fern-1", OrganismType.FERN, 10, 1, "fungal-symbiote");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1, fern1), List.of());
        
        // Count: 1 FUNGUS (2), 1 decomposer (3), 1 symbiote (2 bonus) = 7
        assertEquals(7, garden.fungalContribution());
    }

    @Test
    public void testFungalContributionWithAccelerator() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-accelerator");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1), List.of());
        
        // Count: 1 FUNGUS (2), 1 accelerator (10 bonus) = 12
        assertEquals(12, garden.fungalContribution());
    }
}
