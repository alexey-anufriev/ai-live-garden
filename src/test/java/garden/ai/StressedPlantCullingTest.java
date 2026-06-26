package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class StressedPlantCullingTest {
    @Test
    void stressedPlantsLoseEnergyInNutrientScarcity() {
        // Environment does not favor plants (light, moisture, warmth < 40)
        // Nutrients = 0 (scarcity)
        Environment env = new Environment(10, 10, 10, 0, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        Garden garden = new Garden(0, 2, env, List.of(moss), List.of());

        // Cycle 1: Should get "stressed" and lose 1 energy
        Garden next = garden.nextCycle();
        assertThat(next.organisms().get(0).traits()).contains("stressed");
        // Energy: initial 10, +0 growth, -1 stress penalty = 9
        assertThat(next.organisms().get(0).energy()).isEqualTo(9);
    }

    @Test
    void stressedPlantsAreCulledWhenEnergyReachesZero() {
        // Start with 1 energy so it dies in the next cycle
        Environment env = new Environment(10, 10, 10, 0, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 1, 1);
        Garden garden = new Garden(0, 2, env, List.of(moss), List.of());

        // Cycle 1: Should lose 1 energy -> 0, and be culled
        Garden next = garden.nextCycle();
        
        // Verify original moss was culled
        boolean originalMossPresent = next.organisms().stream()
            .anyMatch(o -> o.id().equals("moss-1"));
        assertThat(originalMossPresent).isFalse();
        
        // Verify the culling event was logged
        boolean foundCullingEvent = next.events().stream()
            .anyMatch(e -> e.description().contains("was culled due to chronic environmental stress"));
        assertThat(foundCullingEvent).isTrue();
    }
}
