package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

public class NutrientRecyclingOptimizerTest {

    @Test
    public void testRootContributionWithNutrientRecyclingOptimizer() {
        // Root network count: 1
        // Base bonus (nutrient > 25, buffer < 50): 2
        // Recycler bonus: 2 + (2 * 1) = 4
        // Total contribution should be > base without recycler optimizer
        
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-recycler");
        Organism root2 = Organism.of("root-2", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-recycler", "nutrient-recycling-optimizer");
        
        Environment env = new Environment(50, 50, 50, 50, 10);
        Garden garden1 = new Garden(1, 1, env, List.of(root1), List.of());
        Garden garden2 = new Garden(1, 2, env, List.of(root2), List.of());
        
        assertTrue(garden2.rootContribution() > garden1.rootContribution(), "Recycling optimizer should increase root contribution.");
    }
}
