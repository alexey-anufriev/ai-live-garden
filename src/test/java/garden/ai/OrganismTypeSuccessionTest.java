package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrganismTypeSuccessionTest {

    @Test
    public void testSporeSuccession() {
        Environment highMoistureFavorable = new Environment(50, 70, 50, 50, 50); // buffer 50
        Environment lowMoistureLowBuffer = new Environment(50, 30, 50, 50, 20); // buffer 20
        Environment highBufferOnly = new Environment(50, 30, 50, 50, 70); // buffer 70
        Environment moderateBuffer = new Environment(50, 30, 50, 50, 40); // buffer 40

        // Spore tests:
        // Rule 1: FUNGUS if buffer > 30.
        // Rule 2: MOSS if moisture > 60.
        
        assertEquals(OrganismType.FUNGUS, OrganismType.SPORE.offspringType(0, 0, highMoistureFavorable));
        assertEquals(OrganismType.FUNGUS, OrganismType.SPORE.offspringType(0, 0, highBufferOnly));
        assertEquals(OrganismType.FUNGUS, OrganismType.SPORE.offspringType(0, 0, moderateBuffer));
        assertEquals(OrganismType.SPORE, OrganismType.SPORE.offspringType(0, 0, lowMoistureLowBuffer));
    }

    @Test
    public void testMossSuccession() {
        Environment favorable = new Environment(70, 60, 50, 50, 50);
        Environment poorLight = new Environment(50, 60, 50, 50, 50);
        Environment poorMoisture = new Environment(70, 40, 50, 50, 50);
        assertEquals(OrganismType.FERN, OrganismType.MOSS.offspringType(0, 0, favorable));
        assertEquals(OrganismType.MOSS, OrganismType.MOSS.offspringType(0, 0, poorLight));
        assertEquals(OrganismType.MOSS, OrganismType.MOSS.offspringType(0, 0, poorMoisture));
    }

    @Test
    public void testFernSuccession() {
        Environment lowMoisture = new Environment(50, 20, 50, 50, 50);
        Environment highMoisture = new Environment(50, 50, 50, 50, 50);
        assertEquals(OrganismType.SPORE, OrganismType.FERN.offspringType(0, 0, lowMoisture));
        assertEquals(OrganismType.FERN, OrganismType.FERN.offspringType(0, 0, highMoisture));
    }

    @Test
    public void testHareSuccession() {
        Environment warm = new Environment(50, 50, 60, 50, 50);
        Environment cold = new Environment(50, 50, 30, 50, 50);
        assertEquals(OrganismType.BEETLE, OrganismType.HARE.offspringType(0, 0, warm));
        assertEquals(OrganismType.HARE, OrganismType.HARE.offspringType(0, 0, cold));
    }
}
