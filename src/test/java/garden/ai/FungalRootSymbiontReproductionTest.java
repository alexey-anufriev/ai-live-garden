package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FungalRootSymbiontReproductionTest {
    @Test
    public void testFungalRootSymbiontLowersReproductionThreshold() {
        Environment env = new Environment(50, 50, 50, 50, 50); // nutrients=50, nutrientBuffer=50
        int fungalContribution = 10;
        
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "test");
        Organism symRoot = Organism.of("root-2", OrganismType.ROOT_NETWORK, 10, 1, "fungal-root-symbiont");
        
        int threshold1 = OrganismInteractionCalculator.reproductionThreshold(root, env, fungalContribution, Collections.emptyList());
        int threshold2 = OrganismInteractionCalculator.reproductionThreshold(symRoot, env, fungalContribution, Collections.emptyList());
        
        assertTrue(threshold2 < threshold1, "Symbiont root should have lower reproduction threshold");
        assertTrue(threshold2 == threshold1 - 3, "Threshold should be reduced by 3");
    }
}
