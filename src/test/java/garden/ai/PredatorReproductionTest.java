package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PredatorReproductionTest {

    @Test
    public void testFoxReproductionThreshold() {
        // Create a fox with 17 energy, should not reproduce if threshold is 18
        List<Organism> organisms = new ArrayList<>();
        organisms.add(Organism.of("fox-1", OrganismType.FOX, 17, 8, "test"));
        
        // Need a garden environment where reproduction can occur.
        // We'll set nutrients high to avoid starvation issues
        Garden garden = new Garden(0, 2, new Environment(100, 100, 100, 100, 100), organisms, List.of());
        
        // Advance 1 cycle
        Garden next = garden.nextCycle();
        
        // If threshold is 18, and energy is 17 (minus metabolism 2 = 15), 15 < 18, no reproduction.
        // If threshold is 15, 15 >= 15, reproduction!
        
        long foxCount = next.organisms().stream().filter(o -> o.type() == OrganismType.FOX).count();
        // Now expect 2 if reproduction occurred
        assertEquals(2, foxCount, "Fox should reproduce with 17 energy if threshold is 15");
    }
}
