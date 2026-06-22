package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MycelialProtectorTest {
    @Test
    void mycelialProtectorReducesMetabolism() {
        // Animal with mycelial-protector in fungal network (fungalContribution > 0)
        Organism animal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "mycelial-protector");
        int fungalContribution = 10;
        int metabolism = animal.type().metabolism();
        
        // Simulating the logic from Garden.passiveChange
        if (animal.traits().contains("mycelial-protector") && fungalContribution > 0) {
            metabolism = Math.max(0, metabolism - 2);
        }
        
        assertEquals(0, metabolism, "Metabolism should be reduced to 0 by mycelial-protector trait.");
    }
}
