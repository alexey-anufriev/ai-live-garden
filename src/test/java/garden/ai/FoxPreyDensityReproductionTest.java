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
        
        // Setup: 3000 beetles (should trigger -5 reduction)
        List<Organism> organisms3000 = new ArrayList<>();
        organisms3000.add(fox);
        for (int i = 0; i < 3000; i++) {
            organisms3000.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 5, 1, "none"));
        }
        assertEquals(10, OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms3000), "Reproduction threshold for FOX with 3000 beetles should be 10.");

        // Setup: 5000 beetles (should trigger -8 reduction)
        List<Organism> organisms5000 = new ArrayList<>();
        organisms5000.add(fox);
        for (int i = 0; i < 5000; i++) {
            organisms5000.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 5, 1, "none"));
        }
        assertEquals(7, OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms5000), "Reproduction threshold for FOX with 5000 beetles should be 7.");
        
        // Setup: 600 beetles (should trigger -3 reduction)
        List<Organism> organisms600 = new ArrayList<>();
        organisms600.add(fox);
        for (int i = 0; i < 600; i++) {
            organisms600.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 5, 1, "none"));
        }
        assertEquals(12, OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms600), "Reproduction threshold for FOX with 600 beetles should be 12.");

        // Setup: 300 beetles (should trigger -2 reduction)
        List<Organism> organisms300 = new ArrayList<>();
        organisms300.add(fox);
        for (int i = 0; i < 300; i++) {
            organisms300.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 5, 1, "none"));
        }
        assertEquals(13, OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms300), "Reproduction threshold for FOX with 300 beetles should be 13.");
        
        // Setup: 150 beetles (should trigger -1 reduction)
        List<Organism> organisms150 = new ArrayList<>();
        organisms150.add(fox);
        for (int i = 0; i < 150; i++) {
            organisms150.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 5, 1, "none"));
        }
        assertEquals(14, OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms150), "Reproduction threshold for FOX with 150 beetles should be 14.");

        // Setup: 7000 beetles (should trigger -10 reduction)
        List<Organism> organisms7000 = new ArrayList<>();
        organisms7000.add(fox);
        for (int i = 0; i < 7000; i++) {
            organisms7000.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 5, 1, "none"));
        }
        assertEquals(5, OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms7000), "Reproduction threshold for FOX with 7000 beetles should be 5.");
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
