package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class NutrientDemandRegulatorTest {
    @Test
    void rootNetworkWithDemandRegulatorReducesConsumption() {
        List<Organism> organisms = new java.util.ArrayList<>();
        // 20 plants
        for (int i = 0; i < 20; i++) {
            organisms.add(Organism.of("plant-" + i, OrganismType.MOSS, 10, 1));
        }
        // 2 root networks with "nutrient-demand-regulator"
        for (int i = 0; i < 2; i++) {
            organisms.add(Organism.of("root-" + i, OrganismType.ROOT_NETWORK, 10, 1, "nutrient-demand-regulator"));
        }
        
        Garden garden = new Garden(1, 100, new Environment(50, 50, 50, 50, 50), organisms, List.of());
        Garden next = garden.nextCycle();

        // 20 plants. 2 demand regulators (count=2, so reduction=2/2 = 1).
        // Expected consumption: (20 / 5) - (0) - 1 = 3.
        // Environment nutrients = 50 + 2 (prod) - 3 (cons) = 49 (should be 49 + releasedFromBuffer).
        // ReleaseRate = 10. Buffer = 50. Release = 5.
        // Nutrients = 49 + 5 = 54.
        
        assertThat(next.environment().nutrients()).isEqualTo(54);
    }
}
