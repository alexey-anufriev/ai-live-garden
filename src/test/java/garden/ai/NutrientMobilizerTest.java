package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NutrientMobilizerTest {
    @Test
    void mobilizerIncreasesNutrientRelease() {
        // Nutrients=50, buffer=50.
        // releaseRate = 10 (without mobilizer).
        // With 1 mobilizer: releaseRate = 10 - 2 = 8.
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Without mobilizer: release = 50 / 10 = 5.
        Environment nextNoMobilizer = env.next(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        
        // With mobilizer: release = 50 / 8 = 6.25 -> 6.
        Environment nextWithMobilizer = env.next(1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0);
        
        assertThat(nextWithMobilizer.nutrients()).isGreaterThan(nextNoMobilizer.nutrients());
    }
}
