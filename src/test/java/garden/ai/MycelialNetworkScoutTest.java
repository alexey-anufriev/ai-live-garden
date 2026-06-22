package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MycelialNetworkScoutTest {

    @Test
    void mycelialNetworkScoutPrioritizesPreyWhenFungusPresent() {
        // Create an environment with fungus contribution > 0
        Environment environment = new Environment(50, 50, 50, 50, 100);
        
        // Hunter with mycelial-network-scout
        Organism hunter = Organism.of("hunter-1", OrganismType.FOX, 10, 1, "mycelial-network-scout");
        
        // Prey
        Organism prey = Organism.of("prey-1", OrganismType.HARE, 5, 1);
        
        // FUNGUS present to enable scout
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        
        List<Organism> organisms = List.of(hunter, prey, fungus);
        
        // Need to simulate a cycle to check for the scouting event
        Garden garden = new Garden(1, 1, environment, organisms, new java.util.ArrayList<>());
        
        // Advance one cycle and inspect events
        Garden advanced = garden.nextCycle();
        
        boolean scouted = advanced.events().stream()
                .anyMatch(e -> e.description().contains("scouted prey using the fungal network"));
        
        assertTrue(scouted, "Hunter should have scouted prey using fungal network");
    }
}
