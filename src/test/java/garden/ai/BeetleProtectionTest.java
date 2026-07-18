package garden.ai;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;

public class BeetleProtectionTest {

    @Test
    public void testLoneBeetleIsProtectedFromFox() {
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "test-beetle");
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 20, 1, "test-fox");
        
        List<Organism> organisms = new ArrayList<>();
        organisms.add(beetle);
        organisms.add(fox);
        
        Environment env = new Environment(100, 100, 100, 100, 100);
        
        // Advance one cycle
        Garden garden = new Garden(1, 10, env, organisms, new ArrayList<>());
        Garden nextGarden = OrganismInteractionCalculator.advance(garden);
        
        // Check if the beetle is still alive
        boolean beetleAlive = nextGarden.organisms().stream().anyMatch(o -> o.type() == OrganismType.BEETLE);
        
        assertTrue(beetleAlive, "The lone beetle should be protected from the fox, but it was eaten.");
    }
}
