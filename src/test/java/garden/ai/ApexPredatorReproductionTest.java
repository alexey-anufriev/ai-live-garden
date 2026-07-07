package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApexPredatorReproductionTest {

    @Test
    public void testApexPredatorReproductionThreshold() {
        // Threshold is 15. Apex-predator modifier is -3, so threshold should be 12.
        // Energy 14. Metabolism 2. 14 - 2 = 12. 12 >= 12, reproduction!
        
        List<Organism> organisms = new ArrayList<>();
        // Apex-predator with 14 energy
        organisms.add(Organism.of("fox-1", OrganismType.FOX, 14, 8, "apex-predator"));
        
        Garden garden = new Garden(0, 2, new Environment(100, 100, 100, 100, 100), organisms, List.of());
        
        // Advance 1 cycle
        Garden next = garden.nextCycle();
        
        long foxCount = next.organisms().stream().filter(o -> o.type() == OrganismType.FOX).count();
        assertEquals(2, foxCount, "Apex predator should reproduce with 14 energy (threshold 12)");
    }
}
