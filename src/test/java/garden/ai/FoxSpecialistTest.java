package garden.ai;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxSpecialistTest {
    @Test
    public void testFoxSpecialistBiteSize() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "fox-specialist");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4, "test");
        java.util.List<Organism> allOrganisms = new java.util.ArrayList<>();
        allOrganisms.add(fox);
        allOrganisms.add(beetle);
        for(int i = 0; i < 500; i++) {
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 4, "test"));
        }
        Environment env = new Environment(50, 50, 50, 50, 50);
        // FOX base = 3. fox-specialist = +2. Total = 5.
        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, allOrganisms);
        assertEquals(5, result.biteSize(), "Fox specialist should have a bite size of 5");
    }
}
