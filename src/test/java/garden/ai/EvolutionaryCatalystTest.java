package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class EvolutionaryCatalystTest {

    @Test
    void testRootNetworkAcquiresFungalSymbiote() {
        // A stressed root network
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 20, 5, "stressed");
        Garden garden = new Garden(0, 2, new Environment(50, 50, 50, 0, 50), List.of(root), List.of());
        
        // Advance enough cycles to trigger mutation
        Garden next = garden;
        boolean acquired = false;
        for (int i = 0; i < 200; i++) { // Increased cycles
            next = next.nextCycle();
            if (next.events().stream().anyMatch(e -> e.description().contains("fungal-symbiote"))) {
                acquired = true;
                break;
            }
        }
        
        assertTrue(acquired, "Stressed ROOT_NETWORK should have acquired fungal-symbiote trait. Events count: " + next.events().size());
    }
}
