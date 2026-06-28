package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmergencyHerbivoreIntroductionTest {

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        System.setProperty("disable.emergency.colonization", "false");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setProperty("disable.emergency.colonization", "true");
    }

    @Test
    public void testEmergencyHerbivoreIntroduction() {
        // Create a garden with > 200 plants and 0 animals
        List<Organism> organisms = new ArrayList<>();
        IntStream.range(0, 201).forEach(i -> 
            organisms.add(Organism.of("moss-" + i, OrganismType.MOSS, 5, 1, "test")));
        
        Garden garden = new Garden(0, 1, new Environment(50, 50, 50, 50, 50), organisms, List.of());
        
        // Advance one cycle - force the herbivore introduction by high likelihood
        // We know the logic uses new Random().nextInt(20) == 0 (5%).
        // Running it enough times should trigger it.
        Garden next = null;
        for (int i = 0; i < 1000; i++) {
            next = garden.nextCycle();
            if (next.organisms().stream().anyMatch(o -> o.type() == OrganismType.BEETLE)) {
                break;
            }
        }
        
        // Verify herbivore was added
        assertTrue(next.organisms().stream().anyMatch(o -> o.type() == OrganismType.BEETLE), 
                "No BEETLE herbivore introduced");
        
        // Verify the event was logged
        assertTrue(next.events().stream().anyMatch(e -> e.description().contains("colonize")), "Colonization event not logged");
    }
}
