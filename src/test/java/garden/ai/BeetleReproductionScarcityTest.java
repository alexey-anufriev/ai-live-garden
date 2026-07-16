package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeetleReproductionScarcityTest {
    @Test
    public void testBeetleThresholdInScarcity() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        List<Organism> allOrganisms = new ArrayList<>();
        Organism beetle1 = Organism.of("beetle-1", OrganismType.BEETLE, 10, 2, "test");
        allOrganisms.add(beetle1);
        
        // Should be 4 because beetleCount (1) < 100
        int threshold = OrganismInteractionCalculator.reproductionThreshold(beetle1, env, 0, allOrganisms);
        assertEquals(4, threshold, "Beetle threshold should be 4 when population is low (< 100)");
    }

    @Test
    public void testBeetleThresholdInNormalPop() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        List<Organism> allOrganisms = new ArrayList<>();
        for(int i = 0; i < 200; i++) {
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 2, "test"));
        }
        Organism beetle1 = allOrganisms.get(0);
        
        // Should be 6 because 100 < beetleCount (200) < 500
        int threshold = OrganismInteractionCalculator.reproductionThreshold(beetle1, env, 0, allOrganisms);
        assertEquals(6, threshold, "Beetle threshold should be 6 when population is normal (100-500)");
    }
}
