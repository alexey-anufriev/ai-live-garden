package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxReproductiveConstraintTest {

    @Test
    public void testFoxReproductionThresholdWithStressOrStarvation() {
        Environment env = new Environment(50, 50, 50, 30, 30); // Mid nutrients/buffer
        
        // Setup a single fox with "starving" trait
        List<Organism> organisms = new ArrayList<>();
        Organism starvingFox = Organism.of("fox-starving", OrganismType.FOX, 5, 1, "starving");
        organisms.add(starvingFox);
        
        int threshold = OrganismInteractionCalculator.reproductionThreshold(starvingFox, env, 0, organisms);
        
        // Base threshold for FOX is 15.
        // If starving, it should increase by 500 => 515.
        assertEquals(515, threshold, "Threshold should increase by 500 for starving foxes");
    }
}
