package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmergencyPredatorIntroductionTest {

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        System.setProperty("disable.emergency.colonization", "false");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setProperty("disable.emergency.colonization", "true");
    }

    @Test
    public void testEmergencyPredatorIntroduction() {
        // Create a garden with herbivores but no predators
        List<Organism> organisms = new ArrayList<>();
        organisms.add(Organism.of("beetle-1", OrganismType.BEETLE, 5, 2, "test"));
        
        Garden garden = new Garden(0, 2, new Environment(50, 50, 50, 50, 50), organisms, List.of());
        
        // Advance cycles - force the predator introduction by high likelihood
        Garden next = null;
        for (int i = 0; i < 2000; i++) {
            next = garden.nextCycle();
            if (next.organisms().stream().anyMatch(o -> o.type() == OrganismType.FOX)) {
                break;
            }
            garden = next;
        }
        
        // Verify predator was added
        assertTrue(next.organisms().stream().anyMatch(o -> o.type() == OrganismType.FOX), 
                "No FOX predator introduced");
        
        // Verify the event was logged
        assertTrue(next.events().stream().anyMatch(e -> e.description().contains("colonize")), "Colonization event not logged");
    }

    @Test
    public void testMultiplePredatorIntroduction() {
        // Create a garden with herbivores and one predator
        List<Organism> organisms = new ArrayList<>();
        organisms.add(Organism.of("beetle-1", OrganismType.BEETLE, 5, 2, "test"));
        organisms.add(Organism.of("fox-1", OrganismType.FOX, 5, 8, "test"));
        
        Garden garden = new Garden(0, 3, new Environment(50, 50, 50, 50, 50), organisms, List.of());
        
        // Advance cycles - force the predator introduction
        Garden next = null;
        for (int i = 0; i < 5000; i++) {
            next = garden.nextCycle();
            if (next.organisms().stream().filter(o -> o.type() == OrganismType.FOX).count() >= 2) {
                break;
            }
            garden = next;
        }
        
        // Verify more than one predator was added
        assertTrue(next.organisms().stream().filter(o -> o.type() == OrganismType.FOX).count() >= 2, 
                "Multiple FOX predators not introduced");
    }
}
