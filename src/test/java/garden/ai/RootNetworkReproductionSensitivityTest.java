package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

public class RootNetworkReproductionSensitivityTest {

    @Test
    public void testHighBufferReducesReproductionThreshold() {
        // Nutrients = 50, Buffer = 100
        Environment highBufferEnv = new Environment(100, 100, 100, 50, 100);
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1);
        
        // Nutrients = 50, Buffer = 10
        Environment lowBufferEnv = new Environment(100, 100, 100, 50, 10);
        
        // Ensure global modifier is NOT triggered by making nutrients <= 75
        
        int baseThreshold = OrganismInteractionCalculator.reproductionThreshold(root, lowBufferEnv, 0, List.of(root));
        int highBufferThreshold = OrganismInteractionCalculator.reproductionThreshold(root, highBufferEnv, 0, List.of(root));
        
        assertTrue(baseThreshold > highBufferThreshold, "High buffer should reduce threshold, but was: base=" + baseThreshold + ", high=" + highBufferThreshold);
    }
}
