package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FungusRootSymbiosisReproductionTest {
    @Test
    public void testFungalRootSymbiontLowersFungusReproductionThresholdWhenRootsArePresent() {
        Environment env = new Environment(50, 50, 50, 50, 50); // nutrients=50, nutrientBuffer=50
        int fungalContribution = 10;
        
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "test");
        Organism symFungus = Organism.of("fungus-2", OrganismType.FUNGUS, 10, 1, "fungal-root-symbiont");
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "test");
        
        List<Organism> organismsWithRoots = List.of(fungus, symFungus, root);
        List<Organism> organismsWithoutRoots = List.of(fungus, symFungus);
        
        int threshold1 = OrganismInteractionCalculator.reproductionThreshold(symFungus, env, fungalContribution, organismsWithRoots);
        int threshold2 = OrganismInteractionCalculator.reproductionThreshold(symFungus, env, fungalContribution, organismsWithoutRoots);
        
        assertTrue(threshold1 < threshold2, "Symbiont fungus should have lower reproduction threshold when roots are present");
        assertTrue(threshold1 == threshold2 - 3, "Threshold should be reduced by 3 when roots are present");
    }
}
