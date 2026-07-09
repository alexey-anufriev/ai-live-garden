package garden.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class FungalDecompositionReproductionTest {
    @Test
    public void testFungalDecompositionEfficiencyReducesReproductionThreshold() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-decomposition-efficiency");
        Environment env = new Environment(100, 100, 100, 30, 30);
        int fungalContribution = 10;
        
        // Reproduction threshold is normally 12 for FUNGUS
        // With "fungal-decomposition-efficiency", it should be 12 - 4 = 8.
        int threshold = OrganismInteractionCalculator.reproductionThreshold(fungus, env, fungalContribution);
        assertEquals(8, threshold);
    }
}
