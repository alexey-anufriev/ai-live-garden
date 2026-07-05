package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

public class FungalContributionTest {

    @Test
    public void testFungalContributionWithConnector() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-network-connector");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1), List.of());
        
        // Count: 1 FUNGUS (2 * 2 = 4), 1 connector (4 * 2 = 8) = 12
        assertEquals(12, garden.fungalContribution());
    }

    @Test
    public void testFungalContributionWithConnectorAndRootNetwork() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-network-connector");
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-weaver");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1, root1), List.of());
        
        // Count: 1 FUNGUS (2 * 2 = 4), 1 connector (6 bonus * 2 = 12) = 16
        assertEquals(16, garden.fungalContribution());
    }

    @Test
    public void testFungalContributionWithSymbiote() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "nutrient-decomposer");
        Organism fern1 = Organism.of("fern-1", OrganismType.FERN, 10, 1, "fungal-symbiote");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1, fern1), List.of());
        
        // Count: 1 FUNGUS (2 * 2 = 4), 1 decomposer (10 * 2 = 20), 1 symbiote (2 bonus) = 26
        assertEquals(26, garden.fungalContribution());
    }

    @Test
    public void testFungalContributionWithAccelerator() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-accelerator");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1), List.of());
        
        // Count: 1 FUNGUS (2 * 2 = 4), 1 accelerator (15 bonus) = 19
        assertEquals(19, garden.fungalContribution());
    }

    @Test
    public void testRootContributionWithFungalRootSymbiont() {
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "fungal-root-symbiont");
        // Environment constructor: Environment(int moisture, int light, int warmth, int nutrients, int nutrientBuffer)
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 4, 10), List.of(root1), List.of());
        
        // Count: 1 ROOT_NETWORK (10), 1 fungal-root-symbiont (40) = 50
        assertEquals(50, garden.rootContribution());
    }

    @Test
    public void testRootContributionWithMultipleTraits() {
        // Nutrients = 4 (nutrients < 5)
        // Traits: fungal-root-symbiont (40), nutrient-weaver (10)
        // Base ROOT_NETWORK: 1 * 10 = 10
        // Total: 10 + 40 + 10 = 60
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "fungal-root-symbiont", "nutrient-weaver");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 4, 10), List.of(root1), List.of());
        
        assertEquals(60, garden.rootContribution());
    }

    @Test
    public void testFungalContributionWithDecomposerAccelerator() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-decomposer-accelerator");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1), List.of());
        
        // Count: 1 FUNGUS (2 * 2 = 4), 1 decomposer-accelerator (30 bonus) = 34
        assertEquals(34, garden.fungalContribution());
    }

    @Test
    public void testFungalContributionWithMycelialSynergizer() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "nutrient-decomposer");
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "mycelial-synergizer");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1, root1), List.of());
        
        // Count: 1 FUNGUS (2 * 2 = 4), 1 decomposer (10 * 2 = 20), 1 synergizer (5 bonus) = 29
        assertEquals(29, garden.fungalContribution());
    }

    @Test
    public void testFungalContributionWithMycelialSynergizerNoFungus() {
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "mycelial-synergizer");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(root1), List.of());
        
        // Should be 0 since no fungi are present to contribute.
        assertEquals(0, garden.fungalContribution());
    }
}
