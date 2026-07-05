package garden.ai;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxSpecialistTest {
    @Test
    public void testFoxSpecialistBiteSize() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "fox-specialist");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 4, "test");
        Environment env = new Environment(50, 50, 50, 50, 50);
        // FOX base = 3. fox-specialist = +2. Total = 5.
        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(fox, beetle, env, 0, 0);
        assertEquals(5, result.biteSize(), "Fox specialist should have a bite size of 5");
    }
}
