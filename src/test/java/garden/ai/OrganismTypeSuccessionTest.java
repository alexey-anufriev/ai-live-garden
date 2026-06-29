package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrganismTypeSuccessionTest {

    private final Environment env = new Environment(50, 50, 50, 50, 50);

    @Test
    public void testSporeSuccession() {
        // (cycle + generation) % 3 == 0 -> MOSS
        assertEquals(OrganismType.MOSS, OrganismType.SPORE.offspringType(0, 0, env));
        assertEquals(OrganismType.SPORE, OrganismType.SPORE.offspringType(1, 0, env));
        assertEquals(OrganismType.SPORE, OrganismType.SPORE.offspringType(2, 0, env));
        assertEquals(OrganismType.MOSS, OrganismType.SPORE.offspringType(3, 0, env));
    }

    @Test
    public void testMossSuccession() {
        // (cycle + generation) % 5 == 0 -> FERN
        assertEquals(OrganismType.FERN, OrganismType.MOSS.offspringType(0, 0, env));
        assertEquals(OrganismType.MOSS, OrganismType.MOSS.offspringType(1, 0, env));
        assertEquals(OrganismType.FERN, OrganismType.MOSS.offspringType(5, 0, env));
    }

    @Test
    public void testFernSuccession() {
        // (cycle + generation) % 9 == 0 -> SPORE
        assertEquals(OrganismType.SPORE, OrganismType.FERN.offspringType(0, 0, env));
        assertEquals(OrganismType.FERN, OrganismType.FERN.offspringType(1, 0, env));
        assertEquals(OrganismType.SPORE, OrganismType.FERN.offspringType(9, 0, env));
    }

    @Test
    public void testHareSuccession() {
        // (cycle + generation) % 7 == 0 -> BEETLE
        assertEquals(OrganismType.BEETLE, OrganismType.HARE.offspringType(0, 0, env));
        assertEquals(OrganismType.HARE, OrganismType.HARE.offspringType(1, 0, env));
        assertEquals(OrganismType.BEETLE, OrganismType.HARE.offspringType(7, 0, env));
    }
}
