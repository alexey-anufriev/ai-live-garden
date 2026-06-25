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
        // released = 100 / 10 = 10.
        // newNutrients = 50 - 18 + 10 = 42.
        Environment next = env.next(1, 100, 0, 0, 0, 0, 0);
        assertThat(next.nutrients()).isEqualTo(42);
    }

    @Test
    void diagnosticWithConsumptionProvidesDetail() {
        Environment env = new Environment(50, 50, 50, 0, 100);
        // Nutrients=0. releaseRate = 2. Released=100/2=50.
        // plantCount=1000, consumptionReduction=0. Consumption = 1000/5=200.
        assertThat(env.diagnostic(1000, 0)).isEqualTo("buffer-supported (nutrients=0, buffer=100, release=50, consumption=200)");
    }

}
