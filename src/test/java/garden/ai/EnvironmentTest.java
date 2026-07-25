package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentTest {
    @Test
    void nextCalculatesNutrientsCorrectly() {
        Environment env = new Environment(50, 50, 50, 50, 100);
        // plantCount=100, animalCount=0.
        // nutrientDelta = 2 + 0/2 - 100/5 = 2 - 20 = -18.
        // releaseRate = 1.
        // released = 100 / 1 = 100.
        // newNutrients = 50 - 18 + 100 = 132, clamped to 100.
        Environment next = env.next(1, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrients()).isEqualTo(100);
    }

    @Test
    void bufferReleasesFasterWhenHigh() {
        Environment env = new Environment(50, 50, 50, 50, 100);
        // releaseRate = 1. Released = 100.
        Environment nextHigh = env.next(1, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(nextHigh.nutrientBuffer()).isEqualTo(0);

        Environment envLow = new Environment(50, 50, 50, 50, 50);
        // releaseRate = 10. Buffer <= 80, rate = 10. Released = 5.
        Environment nextLow = envLow.next(1, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(nextLow.nutrientBuffer()).isEqualTo(45);
    }

    @Test
    void bufferReleasesMuchFasterWhenVeryHigh() {
        Environment env = new Environment(50, 50, 50, 50, 95);
        // releaseRate = 10. With buffer >= 95, rate = 1 (forced). Released = 95 / 1 = 95.
        Environment nextVeryHigh = env.next(1, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(nextVeryHigh.nutrientBuffer()).isEqualTo(0);
    }

    @Test
    void diagnosticWithConsumptionProvidesDetail() {
        Environment env = new Environment(50, 50, 50, 0, 100);
        // Nutrients=0. releaseRate = 2. Released=100/2=50.
        // mossCount=500, fernCount=500, mossReduction=0, fernReduction=0.
        // mossConsumption = 100, fernConsumption = 100, totalConsumption=200.
        // blockedPlantCount = 10.
        // culledPlantCount = 5.
        // stressResilientPlantCount = 0.
        assertThat(env.diagnostic(500, 500, 0, 0, 0, 0, 0, 0, 10, 5, 0)).isEqualTo("buffer-supported (nutrients=0, buffer=100, release=50, consumption=200 [moss=100, fern=100], root-reduction=0, mobilizers=0, releasers=0, accelerators=0, blocked-plants=10, unmet=150, culled=5, stress-resilient=0)");
    }

}
