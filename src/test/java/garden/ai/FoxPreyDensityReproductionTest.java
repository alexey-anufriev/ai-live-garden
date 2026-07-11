package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxPreyDensityReproductionTest {
    @Test
    public void testFoxReproductionThresholdWithHighPreyDensity() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "none");
        
        // Setup: Many beetles (3000)
        List<Organism> organisms = new ArrayList<>();
        organisms.add(fox);
        for (int i = 0; i < 3000; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 5, 1, "none"));
        }
        
        int threshold = OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms);
        
        // Expected: Normally threshold is 15. With high prey (>2000), it should be reduced (e.g., -5 -> 10).
        assertEquals(10, threshold, "Reproduction threshold for FOX with high prey density should be reduced to 10.");
    }

    @Test
    public void testFoxReproductionThresholdWithLowPreyDensity() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "none");
        
        // Setup: Few beetles (100)
        List<Organism> organisms = new ArrayList<>();
        organisms.add(fox);
        for (int i = 0; i < 100; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 5, 1, "none"));
        }
        
        int threshold = OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms);
        
        // Expected: Normally threshold is 15. Should not be reduced.
        assertEquals(15, threshold, "Reproduction threshold for FOX with low prey density should remain 15.");
    }
}
