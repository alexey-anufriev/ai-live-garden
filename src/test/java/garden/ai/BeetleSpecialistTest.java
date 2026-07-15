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
        java.util.List<Organism> allOrganisms = new java.util.ArrayList<>();
        allOrganisms.add(fox);
        allOrganisms.add(beetle);
        for(int i = 0; i < 500; i++) {
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 4, "test"));
        }
        Environment env = new Environment(50, 50, 50, 50, 50);
        // FOX base = 3. beetle-specialist = +2. Total = 5.
        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, allOrganisms);
        assertEquals(5, result.biteSize(), "Beetle specialist fox should have a bite size of 5 when eating beetle");
    }

    @Test
    public void testBeetleSpecialistPrioritizesBeetles() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-specialist");
        Organism hare = Organism.of("hare-1", OrganismType.HARE, 10, 5, "test");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4, "test");
        Environment env = new Environment(50, 50, 50, 50, 50);

        // List is [fox, hare, beetle, 500 other beetles]
        java.util.List<Organism> organisms = new java.util.ArrayList<>();
        organisms.add(fox);
        organisms.add(hare);
        organisms.add(beetle);
        for(int i = 0; i < 500; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 4, "test"));
        }
        // Fox should find beetle first.
        java.util.Optional<Integer> preyIndex = TraitRegistry.findPreyIndex(organisms, fox, 0, 0, env, new java.util.ArrayList<>());
        assertTrue(preyIndex.isPresent());
        assertTrue(preyIndex.get() >= 2, "Should find the beetle");
    }
}
