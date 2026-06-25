package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrganismTypeSuccessionTest {

    @Test
    public void testSporeSuccession() {
        // (cycle + generation) % 3 == 0 -> MOSS
        assertEquals(OrganismType.MOSS, OrganismType.SPORE.offspringType(0, 0));
        assertEquals(OrganismType.SPORE, OrganismType.SPORE.offspringType(1, 0));
        assertEquals(OrganismType.SPORE, OrganismType.SPORE.offspringType(2, 0));
        assertEquals(OrganismType.MOSS, OrganismType.SPORE.offspringType(3, 0));
    }

    @Test
    public void testMossSuccession() {
        // (cycle + generation) % 5 == 0 -> FERN
        assertEquals(OrganismType.FERN, OrganismType.MOSS.offspringType(0, 0));
        assertEquals(OrganismType.MOSS, OrganismType.MOSS.offspringType(1, 0));
        assertEquals(OrganismType.FERN, OrganismType.MOSS.offspringType(5, 0));
    }

    @Test
    public void testFernSuccession() {
        // (cycle + generation) % 9 == 0 -> SPORE
        assertEquals(OrganismType.SPORE, OrganismType.FERN.offspringType(0, 0));
        assertEquals(OrganismType.FERN, OrganismType.FERN.offspringType(1, 0));
        assertEquals(OrganismType.SPORE, OrganismType.FERN.offspringType(9, 0));
    }

    @Test
    public void testHareSuccession() {
        // (cycle + generation) % 7 == 0 -> BEETLE
        assertEquals(OrganismType.BEETLE, OrganismType.HARE.offspringType(0, 0));
        assertEquals(OrganismType.HARE, OrganismType.HARE.offspringType(1, 0));
        assertEquals(OrganismType.BEETLE, OrganismType.HARE.offspringType(7, 0));
    }
}
