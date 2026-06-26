package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmergencyReseedingTest {

    @Test
    public void testEmergencyReseedingDiversity() {
        // Create an empty garden (all organisms die)
        Garden emptyGarden = new Garden(0, 1, new Environment(50, 50, 50, 50, 50), List.of(), List.of());
        
        // Advance one cycle
        Garden next = emptyGarden.nextCycle();
        
        // Verify an organism was added
        assertEquals(1, next.organisms().size());
        
        // Verify it is one of the allowed emergency seed types
        Organism seeded = next.organisms().get(0);
        assertTrue(List.of(OrganismType.MOSS, OrganismType.SPORE, OrganismType.FERN, OrganismType.FUNGUS).contains(seeded.type()),
                "Seeded organism should be one of the plant types, but was " + seeded.type());
        
        // Verify the event was logged
        assertTrue(next.events().stream().anyMatch(e -> e.description().contains("emergency")), "Emergency event not logged");
    }
}
