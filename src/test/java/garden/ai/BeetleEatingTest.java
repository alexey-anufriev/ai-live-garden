package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class BeetleEatingTest {
    @Test
    public void testLoneBeetleEatsPlant() {
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 5, 1, "amber-shell");
        Organism plant = Organism.of("moss-1", OrganismType.MOSS, 10, 1, "test-plant");
        
        List<Organism> organisms = new ArrayList<>();
        organisms.add(beetle);
        organisms.add(plant);
        
        Environment env = new Environment(100, 100, 100, 100, 100);
        
        // Advance one cycle: Metabolism -> Feeding -> Reproduction
        Garden garden = new Garden(1, 10, env, organisms, new ArrayList<>());
        Garden nextGarden = OrganismInteractionCalculator.advance(garden);
        
        // Check if the beetle ate the moss
        long beetleCount = nextGarden.organisms().stream().filter(o -> o.type() == OrganismType.BEETLE).count();
        long plantCount = nextGarden.organisms().stream().filter(o -> o.type() == OrganismType.MOSS).count();
        
        assertTrue(nextGarden.organisms().stream().anyMatch(o -> o.type() == OrganismType.BEETLE && o.energy() > 5), 
            "Beetle should have eaten and gained energy, energy is " + nextGarden.organisms().stream().filter(o -> o.type() == OrganismType.BEETLE).findFirst().get().energy());
    }
}
