package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NutrientMobilizerTest {
    @Test
    void mobilizerIncreasesNutrientRelease() {
        // Nutrients=50, buffer=100.
        // releaseRate = 10 (without mobilizer).
        // With 1 mobilizer: releaseRate = 10 - 1 = 9.
        Environment env = new Environment(50, 50, 50, 50, 100);
        
        // Without mobilizer: release = 100 / 10 = 10.
        Environment nextNoMobilizer = env.next(1, 0, 0, 0, 0, 0, 0, 0);
        
        // With mobilizer: release = 100 / 9 = 11.
        Environment nextWithMobilizer = env.next(1, 0, 0, 0, 0, 0, 1, 0);
        
        assertThat(nextWithMobilizer.nutrients()).isGreaterThan(nextNoMobilizer.nutrients());
    }
}
