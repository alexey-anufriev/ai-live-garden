package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReproductiveEfficiencyTest {

    @Test
    public void testReproductiveEfficiencyThresholdReduction() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 5, 1, "reproductive-efficiency");
        
        int threshold = OrganismInteractionCalculator.reproductionThreshold(fox, env, 0);
        
        // Base threshold for FOX is 15.
        // reproductive-efficiency trait should reduce it by 3.
        // Expected: 15 - 3 = 12.
        assertEquals(12, threshold, "Reproduction threshold should be reduced by 3 for FOX with reproductive-efficiency trait.");
    }
}
