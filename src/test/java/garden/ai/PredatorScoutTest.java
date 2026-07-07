package garden.ai;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
