package garden.ai;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApexPredatorTest {
    @Test
    public void testApexPredatorBiteSize() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "apex-predator");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4, "test");
        Environment env = new Environment(50, 50, 50, 50, 50);
        // FOX base = 3. apex-predator = +3. Total = 6.
        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, List.of(fox, beetle));
        assertEquals(6, result.biteSize(), "Apex predator fox should have a bite size of 6");
    }

    @Test
    public void testApexPredatorIgnoresStealth() {
        // Fox is apex predator.
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "apex-predator");
        // Beetle is camouflaged.
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4, "camouflaged");
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        List<Organism> organisms = List.of(fox, beetle);
        // Fox should find beetle despite camouflage.
        java.util.Optional<Integer> preyIndex = TraitRegistry.findPreyIndex(organisms, fox, 0, 0, env, new java.util.ArrayList<>());
        assertTrue(preyIndex.isPresent(), "Apex predator fox should find camouflaged prey");
        assertEquals(1, preyIndex.get(), "Should find the beetle");
    }
}
