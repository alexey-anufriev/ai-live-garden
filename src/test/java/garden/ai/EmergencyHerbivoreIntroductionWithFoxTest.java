package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmergencyHerbivoreIntroductionWithFoxTest {

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        System.setProperty("disable.emergency.colonization", "false");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setProperty("disable.emergency.colonization", "true");
    }

    @Test
    public void testEmergencyHerbivoreIntroductionWithFox() {
        // Create a garden with > 200 plants, 1 fox, and 0 beetles
        List<Organism> organisms = new ArrayList<>();
        IntStream.range(0, 201).forEach(i -> 
            organisms.add(Organism.of("moss-" + i, OrganismType.MOSS, 5, 1, "test")));
        organisms.add(Organism.of("fox-1", OrganismType.FOX, 10, 8, "test"));
        
        Garden garden = new Garden(0, 1000, new Environment(50, 50, 50, 50, 50), organisms, List.of());
        
        // Advance cycles - force the herbivore introduction by high likelihood
        // With current chance 1/10 per cycle, 1000 cycles is plenty.
        Garden next = null;
        boolean beetleIntroduced = false;
        for (int i = 0; i < 1000; i++) {
            next = garden.nextCycle();
            if (next.organisms().stream().anyMatch(o -> o.type() == OrganismType.BEETLE)) {
                beetleIntroduced = true;
                break;
            }
        }
        
        // Verify herbivore was added
        assertTrue(beetleIntroduced, "No BEETLE herbivore introduced even with foxes present");
        
        // Verify the event was logged
        assertTrue(next.events().stream().anyMatch(e -> e.description().contains("colonize")), "Colonization event not logged");
    }
}
