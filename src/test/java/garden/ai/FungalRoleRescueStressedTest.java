package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FungalRoleRescueStressedTest {
    @Test
    public void testFungusRoleRescueWithStressedRoot() {
        // Create garden with 1 stressed root network and 0 fungus, with enough energy to reproduce
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 20, 5, "listens-below", "stressed");
        Garden garden = new Garden(1, 2, new Environment(50, 64, 43, 58, 50), List.of(root), List.of());
        
        // Advance reproduction
        Garden next = garden.nextCycle();
        
        // Verify fungus was created
        long fungusCount = next.organisms().stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
        assertEquals(1, fungusCount, "Fungus should be rescued even with stressed root network");
    }
}
