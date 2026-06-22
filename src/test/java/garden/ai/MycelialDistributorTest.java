package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MycelialDistributorTest {

    @Test
    public void testMycelialDistributorBoostsBuffer() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 0);
        Organism distributor = Organism.of("hare-1", OrganismType.HARE, 1, 0, "mycelial-distributor");
        // Ensure distributor dies by setting energy to 1, as energy is drained by metabolism
        // Actually, they need 0 energy to die. Let's make sure it dies.
        Organism starvingDistributor = distributor.withEnergy(0); 

        Garden garden = new Garden(0, 100, new Environment(50, 50, 50, 50, 10), List.of(fungus, starvingDistributor), List.of());
        
        Garden next = garden.nextCycle();
        
        // FUNGUS present, so buffer should be boosted
        assertTrue(next.environment().nutrientBuffer() > 10, "Buffer should increase");
        assertEquals(17, next.environment().nutrientBuffer(), "Buffer should increase by 7 (2+5)");
    }
}
