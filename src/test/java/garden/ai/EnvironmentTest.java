package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentTest {
    @Test
    void nextCalculatesNutrientsCorrectly() {
        Environment env = new Environment(50, 50, 50, 50, 100);
        // plantCount=100, animalCount=0.
        // nutrientDelta = 2 + 0/2 - 100/5 = 2 - 20 = -18.
        // releaseRate = 10 (nutrients 50 >= 10).
        // releaseRate = Math.max(1, 10 / 2) = 5 (since buffer=100 > 80).
        // released = 100 / 5 = 20.
        // newNutrients = 50 - 18 + 20 = 52.
        Environment next = env.next(1, 100, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrients()).isEqualTo(52);
    }

    @Test
    void bufferReleasesFasterWhenHigh() {
        Environment env = new Environment(50, 50, 50, 50, 100);
        // releaseRate = 10. With buffer > 80, rate = 5. Released = 20.
        Environment nextHigh = env.next(1, 100, 0, 0, 0, 0, 0, 0, 0);
        assertThat(nextHigh.nutrientBuffer()).isEqualTo(80);

        Environment envLow = new Environment(50, 50, 50, 50, 50);
        // releaseRate = 10. Buffer <= 80, rate = 10. Released = 5.
        Environment nextLow = envLow.next(1, 100, 0, 0, 0, 0, 0, 0, 0);
        assertThat(nextLow.nutrientBuffer()).isEqualTo(45);
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
        assertThat(env.diagnostic(500, 500, 0, 0, 0, 0, 0, 10, 5, 0)).isEqualTo("buffer-supported (nutrients=0, buffer=100, release=50, consumption=200 [moss=100, fern=100], root-reduction=0, mobilizers=0, releasers=0, blocked-plants=10, unmet=150, culled=5, stress-resilient=0)");
    }

}
