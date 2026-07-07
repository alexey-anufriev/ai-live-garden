package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxSpecialistReproductionTest {

    @Test
    public void testFoxSpecialistReproductionThreshold() {
        // Threshold is 15. Fox-specialist modifier is -2, so threshold should be 13.
        // Energy 14. Metabolism 2. 14 - 2 = 12. 12 < 13.
        // Wait, 14 energy, metabolism 2, remaining energy 12. 
        // 12 < 13, so no reproduction.
        // If energy was 15, 15 - 2 = 13. 13 >= 13, reproduction!
        
        List<Organism> organisms = new ArrayList<>();
        // Fox-specialist with 15 energy
        organisms.add(Organism.of("fox-1", OrganismType.FOX, 15, 8, "fox-specialist"));
        
        Garden garden = new Garden(0, 2, new Environment(100, 100, 100, 100, 100), organisms, List.of());
        
        // Advance 1 cycle
        Garden next = garden.nextCycle();
        
        long foxCount = next.organisms().stream().filter(o -> o.type() == OrganismType.FOX).count();
        assertEquals(2, foxCount, "Fox specialist should reproduce with 15 energy (threshold 13)");
    }
}
