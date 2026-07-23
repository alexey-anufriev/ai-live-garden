package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class NutrientConserverTest {
    @Test
    void plantsWithNutrientConserverReduceConsumption() {
        // Create 20 plants, 10 of them have the "nutrient-conserver" trait
        List<Organism> organisms = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            organisms.add(Organism.of("plant-" + i, OrganismType.MOSS, 10, 1));
        }
        for (int i = 0; i < 10; i++) {
            organisms.add(Organism.of("conserver-" + i, OrganismType.MOSS, 10, 1, "nutrient-conserver"));
        }
        
        // Environment nutrients = 50 (high, reduction factor = 5)
        Garden garden = new Garden(1, 100, new Environment(50, 50, 50, 50, 50), organisms, List.of());
        Garden next = garden.nextCycle();

        // 20 plants total, 10 conservers.
        // Expected consumption: (20 / 5) - (10 / 5) = 4 - 2 = 2.
        // Environment nutrients = 50 + 2 (prod) - 2 (cons) + 5 (buffer release) = 55.
        
        assertThat(next.environment().nutrients()).isEqualTo(55);
    }

    @Test
    void plantsWithNutrientConserverReduceConsumptionMoreWhenLowNutrients() {
        // Create 20 plants, 10 of them have the "nutrient-conserver" trait
        List<Organism> organisms = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            organisms.add(Organism.of("plant-" + i, OrganismType.MOSS, 10, 1));
        }
        for (int i = 0; i < 10; i++) {
            organisms.add(Organism.of("conserver-" + i, OrganismType.MOSS, 10, 1, "nutrient-conserver"));
        }
        
        // Environment nutrients = 5 (low, reduction factor = 1)
        Garden garden = new Garden(1, 100, new Environment(50, 50, 50, 5, 50), organisms, List.of());
        Garden next = garden.nextCycle();

        // 20 plants total, 10 conservers.
        // Expected consumption: (20 / 5) - (10 / 1) = 4 - 10 = max(0, -6) = 0.
        // Environment nutrients = 5 + 2 (prod) - 0 (cons) + 25 (buffer release, 50/2) = 32.
        
        assertThat(next.environment().nutrients()).isEqualTo(32);
    }
}
