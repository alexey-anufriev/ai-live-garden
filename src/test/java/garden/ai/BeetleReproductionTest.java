package garden.ai;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;

public class BeetleReproductionTest {

    @Test
    public void testBeetleReproductionPossibility() {
        Environment environment = new Environment(1, 50, 50, 50, 50);
        List<Organism> organisms = new ArrayList<>();
        // Beetle with energy 5
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 5, 1, "beetle-recovery", "prolific", "resourceful-breeder");
        organisms.add(beetle);

        int threshold = OrganismInteractionCalculator.reproductionThreshold(beetle, environment, 0, organisms);
        assertTrue(beetle.energy() >= threshold, "Beetle should have enough energy to reproduce (energy=" + beetle.energy() + ", threshold=" + threshold + ")");
    }
}
