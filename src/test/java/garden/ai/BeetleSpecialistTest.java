package garden.ai;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeetleSpecialistTest {
    @Test
    public void testBeetleSpecialistBiteSize() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-specialist");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4, "test");
        Environment env = new Environment(50, 50, 50, 50, 50);
        // FOX base = 3. beetle-specialist = +2. Total = 5.
        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, List.of(fox, beetle));
        assertEquals(5, result.biteSize(), "Beetle specialist fox should have a bite size of 5 when eating beetle");
    }

    @Test
    public void testBeetleSpecialistPrioritizesBeetles() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-specialist");
        Organism hare = Organism.of("hare-1", OrganismType.HARE, 10, 5, "test");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4, "test");
        Environment env = new Environment(50, 50, 50, 50, 50);

        // List is [fox, hare, beetle]
        List<Organism> organisms = List.of(fox, hare, beetle);
        // Fox should find beetle first, which is at index 2.
        java.util.Optional<Integer> preyIndex = TraitRegistry.findPreyIndex(organisms, fox, 0, 0, env, new java.util.ArrayList<>());
        assertTrue(preyIndex.isPresent());
        assertEquals(2, preyIndex.get(), "Should find the beetle first");
    }
}
