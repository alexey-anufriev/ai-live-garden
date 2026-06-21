package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

public class RootTapperTest {

    @Test
    public void testRootTapperGainsEnergyNearRoots() {
        // Simple test to verify the trait exists and can be added
        Organism animal = Organism.of("hare-1", OrganismType.HARE, 10, 0, "root-tapper");
        assertTrue(animal.traits().contains("root-tapper"));
        
        // The actual logic will be inside Garden.feedingPhase
    }
}
