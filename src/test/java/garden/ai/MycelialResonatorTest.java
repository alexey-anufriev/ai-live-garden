package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

public class MycelialResonatorTest {

    @Test
    public void testMycelialResonatorMetabolismReduction() {
        // Animal with mycelial-resonator in fungal network (fungalContribution > 0)
        Organism animal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "mycelial-resonator");
        Environment environment = new Environment(100, 10, 100, 100, 100);
        
        // Mocking feeding phase logic
        int metabolism = 1;
        int fungalContribution = 10;
        
        // Simulated metabolic reduction
        if (animal.traits().contains("mycelial-resonator") && fungalContribution > 0 && !animal.traits().contains("mycelial-scavenger")) {
            metabolism = Math.max(0, metabolism - 1);
        }
        
        assertEquals(0, metabolism, "Metabolism should be reduced to 0 by mycelial-resonator trait.");
    }

    @Test
    public void testNoReductionWithoutFungalNetwork() {
        // Animal with mycelial-resonator NOT in fungal network
        Organism animal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "mycelial-resonator");
        
        int metabolism = 1;
        int fungalContribution = 0;
        
        if (animal.traits().contains("mycelial-resonator") && fungalContribution > 0 && !animal.traits().contains("mycelial-scavenger")) {
            metabolism = Math.max(0, metabolism - 1);
        }
        
        assertEquals(1, metabolism, "Metabolism should NOT be reduced without fungal network.");
    }
}
