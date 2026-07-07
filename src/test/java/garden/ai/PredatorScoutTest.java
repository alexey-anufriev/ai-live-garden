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
        
        List<Organism> organisms = List.of(fox, stealthyBeetle);
        
        // Cycle 0: id().hashCode() % 2 == 0 for beetle-1 (stealthyBeetle)
        Optional<Integer> preyIndex = TraitRegistry.findPreyIndex(organisms, fox, 0, 0, env, new ArrayList<>());
        
        assertTrue(preyIndex.isPresent(), "Predator scout should find stealthy prey");
    }

    @Test
    public void testPredatorScoutBiteBonus() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "predator-scout");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4);
        Environment env = new Environment(10, 10, 10, 10, 10);
        
        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, new ArrayList<>());
        
        // Base bite for fox is 3. Predator scout adds 1.
        assertEquals(4, result.biteSize(), "Predator scout should increase bite size");
    }
}
