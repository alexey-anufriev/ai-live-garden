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
        // Ensure distributor dies by setting energy to 0
        Organism starvingDistributor = distributor.withEnergy(0); 

        Garden garden = new Garden(0, 100, new Environment(50, 50, 50, 50, 10), List.of(fungus, starvingDistributor), List.of());
        
        Garden next = garden.nextCycle();
        
        // FUNGUS present, so buffer should be boosted
        assertTrue(next.environment().nutrientBuffer() > 10, "Buffer should increase");
        assertEquals(17, next.environment().nutrientBuffer(), "Buffer should increase by 7 (2+5)");
    }

    @Test
    public void testMycelialDistributorDoesNotBoostHighBuffer() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 0);
        Organism distributor = Organism.of("hare-1", OrganismType.HARE, 1, 0, "mycelial-distributor");
        Organism starvingDistributor = distributor.withEnergy(0); 
        Organism noDistributor = Organism.of("hare-2", OrganismType.HARE, 1, 0);

        // Environment starts with buffer at 85
        Garden gardenDistributor = new Garden(0, 100, new Environment(50, 50, 50, 50, 85), List.of(fungus, starvingDistributor), List.of());
        Garden gardenNoDistributor = new Garden(0, 100, new Environment(50, 50, 50, 50, 85), List.of(fungus, noDistributor), List.of());
        
        Garden nextDistributor = gardenDistributor.nextCycle();
        Garden nextNoDistributor = gardenNoDistributor.nextCycle();
        
        // Both should have the same buffer level (drainage happens, but no boost)
        assertEquals(nextNoDistributor.environment().nutrientBuffer(), nextDistributor.environment().nutrientBuffer(), "Buffer level should be the same whether a distributor is present or not when buffer is high.");
    }
}
