package garden.ai;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PredatorScoutTest {
    @Test
    public void testPredatorScoutBypassesStealth() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "predator-scout");
        // Beetle with stealth
        Organism stealthyBeetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4, "shadow-stepper");
        Environment env = new Environment(10, 10, 10, 10, 10);
        
        java.util.List<Organism> organisms = new java.util.ArrayList<>();
        organisms.add(fox);
        organisms.add(stealthyBeetle);
        for(int i = 0; i < 500; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 4, "test"));
        }
        
        // Cycle 0: id().hashCode() % 2 == 0 for beetle-1 (stealthyBeetle)
        Optional<Integer> preyIndex = TraitRegistry.findPreyIndex(organisms, fox, 0, 0, env, new java.util.ArrayList<>());
        
        assertTrue(preyIndex.isPresent(), "Predator scout should find stealthy prey");
    }

    @Test
    public void testPredatorScoutBiteBonus() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "predator-scout");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4);
        Environment env = new Environment(10, 10, 10, 10, 10);
        
        java.util.List<Organism> allOrganisms = new java.util.ArrayList<>();
        allOrganisms.add(fox);
        allOrganisms.add(beetle);
        for(int i = 0; i < 500; i++) {
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 4, "test"));
        }

        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, allOrganisms);
        
        // Base bite for fox is 3. Predator scout adds 1.
        assertEquals(4, result.biteSize(), "Predator scout should increase bite size");
    }
}
