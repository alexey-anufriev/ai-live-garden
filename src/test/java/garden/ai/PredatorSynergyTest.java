package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

public class PredatorSynergyTest {

    @Test
    public void testPredatorSynergyBiteSize() {
        Organism fox1 = Organism.of("fox-1", OrganismType.FOX, 10, 8, "predator-synergy");
        Organism fox2 = Organism.of("fox-2", OrganismType.FOX, 10, 8, "test");
        Organism fox3 = Organism.of("fox-3", OrganismType.FOX, 10, 8, "test");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4, "test");
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        java.util.List<Organism> allOrganisms = new java.util.ArrayList<>();
        allOrganisms.add(fox1);
        allOrganisms.add(fox2);
        allOrganisms.add(fox3);
        allOrganisms.add(beetle);
        for(int i = 0; i < 500; i++) {
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 4, "test"));
        }
        
        // FOX base = 3. Synergy with 2 other foxes = +2. Total = 5.
        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(fox1, beetle, env, 0, 0, allOrganisms);
        assertEquals(5, result.biteSize(), "Predator with synergy and 2 other foxes should have bite size 5");
        assertTrue(result.events().stream().anyMatch(e -> e.description().contains("synergy")), "Should have a synergy event");
    }
}
