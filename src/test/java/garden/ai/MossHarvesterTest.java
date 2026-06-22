package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MossHarvesterTest {

    @Test
    void mossHarvesterReducesMetabolismWhenMossPresent() {
        // Create an environment with mosses
        Environment environment = new Environment(50, 50, 50, 50, 100);
        
        // Animal with moss-harvester
        Organism animal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "moss-harvester");
        
        // MOSS present
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        List<Organism> organisms = List.of(animal, moss);
        
        // Advance one cycle
        Garden garden = new Garden(1, 2, environment, organisms, new java.util.ArrayList<>());
        
        // Advance one cycle and inspect events
        Garden advanced = garden.nextCycle();
        
        boolean harvested = advanced.events().stream()
                .anyMatch(e -> e.description().contains("harvested nutrients from mosses"));
        
        assertTrue(harvested, "Animal should have harvested nutrients from mosses");
    }
}
