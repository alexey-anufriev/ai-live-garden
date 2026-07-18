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

    @Test
    public void testBeetlePopulationBetween20And200IsProtectedFromFox() {
        List<Organism> organisms = new ArrayList<>();
        // Add 50 beetles
        for (int i = 0; i < 50; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 1, "test-beetle"));
        }
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 20, 1, "test-fox");
        organisms.add(fox);
        
        Environment env = new Environment(100, 100, 100, 100, 100);
        
        // Advance one cycle
        Garden garden = new Garden(1, 100, env, organisms, new ArrayList<>());
        Garden nextGarden = OrganismInteractionCalculator.advance(garden);
        
        // Check if the beetle count is at least 50
        long beetleCount = nextGarden.organisms().stream().filter(o -> o.type() == OrganismType.BEETLE).count();
        
        assertTrue(beetleCount >= 50, "Beetle population between 20 and 200 should be protected, but count changed to " + beetleCount);
    }
}
