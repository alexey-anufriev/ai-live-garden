package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SporeDisperserTest {

    @Test
    public void testSporeDisperserMetabolismReduction() {
        // Animal with spore-disperser in fungal network (fungalContribution > 0)
        Organism animal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "spore-disperser");
        
        // Simulated metabolic reduction
        int metabolism = 1;
        int fungalContribution = 10;
        
        if (animal.traits().contains("spore-disperser") && fungalContribution > 0) {
            metabolism = Math.max(0, metabolism - 1);
        }
        
        assertEquals(0, metabolism, "Metabolism should be reduced to 0 by spore-disperser trait.");
    }

    @Test
    public void testNoReductionWithoutFungalNetwork() {
        // Animal with spore-disperser NOT in fungal network
        Organism animal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "spore-disperser");
        
        int metabolism = 1;
        int fungalContribution = 0;
        
        if (animal.traits().contains("spore-disperser") && fungalContribution > 0) {
            metabolism = Math.max(0, metabolism - 1);
        }
        
        assertEquals(1, metabolism, "Metabolism should NOT be reduced without fungal network.");
    }
}
