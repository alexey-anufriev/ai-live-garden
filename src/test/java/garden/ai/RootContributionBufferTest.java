package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

public class RootContributionBufferTest {

    @Test
    public void testHighBufferContributionBonus() {
        // Nutrients = 50 (>= 25)
        // Buffer = 100 (> 50)
        // Traits: buffer-optimizer
        
        // RootNetwork (1) + BufferOptimizer (2 * BONUS)
        
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "buffer-optimizer");
        Garden gardenLowBuffer = new Garden(1, 1, new Environment(50, 50, 50, 50, 10), List.of(root1), List.of());
        int lowBufferContribution = gardenLowBuffer.rootContribution();
        
        Garden gardenHighBuffer = new Garden(1, 1, new Environment(50, 50, 50, 50, 100), List.of(root1), List.of());
        int highBufferContribution = gardenHighBuffer.rootContribution();
        
        assertTrue(highBufferContribution > lowBufferContribution, "High buffer should increase contribution, high=" + highBufferContribution + ", low=" + lowBufferContribution);
    }
}
