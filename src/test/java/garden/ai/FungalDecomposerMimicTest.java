package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FungalDecomposerMimicTest {

    @Test
    public void testFungalDecomposerMimicContribution() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 5, 5, "test")
            .withTrait("fungal-decomposer-mimic");
        
        Garden garden = new Garden(1, 2, new Environment(50, 50, 50, 50, 50), List.of(root), List.of());
        
        // Fungal contribution should be 5 (mimic count 1 * 5)
        assertEquals(5, garden.fungalContribution());
    }
}
