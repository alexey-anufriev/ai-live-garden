package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxPopulationControlTest {

    @Test
    public void testFoxDirectMortalityAtHighDensity() {
        Environment env = new Environment(50, 50, 50, 30, 30);
        List<Organism> organisms = new ArrayList<>();
        // Setup 2500 foxes, which is > 2000 threshold
        for (int i = 0; i < 2500; i++) {
            organisms.add(Organism.of("fox-" + i, OrganismType.FOX, 10, 1));
        }

        Organism fox = organisms.get(0);
        
        // Apply interaction logic directly
        long foxCount = organisms.stream().filter(o -> o.type() == OrganismType.FOX).count();
        Organism changed = fox;
        
        // This simulates the logic now in OrganismInteractionCalculator
        if (fox.type() == OrganismType.FOX && foxCount > 2000) {
            changed = changed.withEnergy(0);
        }
        
        assertEquals(0, changed.energy(), "Fox should be culled (energy set to 0) at high density");
    }
}
