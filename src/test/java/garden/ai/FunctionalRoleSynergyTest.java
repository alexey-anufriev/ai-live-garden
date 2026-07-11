package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

public class FunctionalRoleSynergyTest {

    @Test
    public void testFoxSynergyWithRoots() {
        Environment env = new Environment(100, 100, 100, 20, 20); // Low buffer/nutrients to avoid global modifiers
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "mutualist-synergy");
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1);
        
        // No roots (root contribution is 0)
        int thresholdNoRoots = OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, List.of(fox));
        // With roots (root network count is 1)
        int thresholdWithRoots = OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, List.of(fox, root));
        
        assertTrue(thresholdNoRoots > thresholdWithRoots, "Synergy with roots should reduce threshold, but was: noRoots=" + thresholdNoRoots + ", withRoots=" + thresholdWithRoots);
    }
}
