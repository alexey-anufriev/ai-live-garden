package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

public class NutrientAcceleratorTest {

    @Test
    public void testRootContributionWithNutrientAccelerator() {
        // Test with nutrients < 5 (accelerator bonus 50)
        // ROOT_NETWORK (10) + accelerator (50) = 60
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-accelerator");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 4, 10), List.of(root1), List.of());
        assertEquals(60, garden.rootContribution());
    }

    @Test
    public void testRootContributionWithNutrientAcceleratorHighNutrients() {
        // Test with nutrients >= 25 (accelerator bonus 5)
        // ROOT_NETWORK (Math.max(1, count/2) = 1) + accelerator (5) = 6
        Organism root1 = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-accelerator");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 10), List.of(root1), List.of());
        assertEquals(6, garden.rootContribution());
    }
}
