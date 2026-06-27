package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class BufferReleaserTest {
    @Test
    void bufferReleaserPlantsIncreaseNutrientRelease() {
        // Nutrient buffer = 100. Base release rate for nutrients=0 is 2.
        // Released = 100 / 2 = 50.
        // With one buffer-releaser, release rate = max(1, 2 - 1) = 1.
        // Released = 100 / 1 = 100.
        Environment env = new Environment(50, 50, 50, 0, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1, "buffer-releaser");
        Garden garden = new Garden(0, 2, env, List.of(moss), List.of());

        Garden next = garden.nextCycle();
        
        // Check if nutrient buffer release was boosted
        // The event log should show the stats.
        boolean foundBoost = next.events().stream()
            .anyMatch(e -> e.description().contains("effectiveRate=1"));
        assertThat(foundBoost).isTrue();
    }
}
