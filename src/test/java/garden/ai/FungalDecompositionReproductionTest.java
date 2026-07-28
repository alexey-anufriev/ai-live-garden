package garden.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class FungalDecompositionReproductionTest {
    @Test
    public void testFungalDecompositionEfficiencyReducesReproductionThreshold() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-decomposition-efficiency");
        Environment env = new Environment(100, 100, 100, 30, 30);
        int fungalContribution = 10;
        
        // Reproduction threshold is normally 8 for FUNGUS
        // With "fungal-decomposition-efficiency", it should be 8 - 8 = 0.
        int threshold = OrganismInteractionCalculator.reproductionThreshold(fungus, env, fungalContribution, java.util.Collections.emptyList());
        assertEquals(0, threshold);
    }

    @Test
    public void testFungalDecompositionEfficiencyHighBufferReducesReproductionThresholdFurther() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-decomposition-efficiency");
        // Nutrients 100, Buffer 80 (High Buffer > 75)
        Environment env = new Environment(100, 100, 100, 100, 80);
        int fungalContribution = 10;
        // Reproduction threshold normally 8 for FUNGUS
        // Base 8 - 4 (buffer > 50) - 2 (nutrients > 60) - 12 (modifier) - 5 (global modifier) = -15.
        int threshold = OrganismInteractionCalculator.reproductionThreshold(fungus, env, fungalContribution, java.util.Collections.emptyList());
        assertEquals(-15, threshold);
    }
}
