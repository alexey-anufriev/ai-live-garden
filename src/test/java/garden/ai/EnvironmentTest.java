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
        // released = 100 / 2 = 50.
        // newNutrients = 50 - 18 + 50 = 82.
        Environment next = env.next(1, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrients()).isEqualTo(82);
    }

    @Test
    void bufferReleasesFasterWhenHigh() {
        Environment env = new Environment(50, 50, 50, 50, 100);
        // releaseRate = 1. Released = 100/2 = 50.
        Environment nextHigh = env.next(1, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(nextHigh.nutrientBuffer()).isEqualTo(50);

        Environment envLow = new Environment(50, 50, 50, 50, 50);
        // releaseRate = 10. Buffer <= 80, rate = 10. Released = 50/10 = 5.
        Environment nextLow = envLow.next(1, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(nextLow.nutrientBuffer()).isEqualTo(45);
    }

    @Test
    void bufferReleasesMuchFasterWhenVeryHigh() {
        Environment env = new Environment(50, 50, 50, 50, 95);
        // releaseRate = 2 (forced). Released = 95 / 2 = 47.
        Environment nextVeryHigh = env.next(1, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(nextVeryHigh.nutrientBuffer()).isEqualTo(48);
    }

    @Test
    void bufferAccumulatesFasterWhenEmpty() {
        Environment env = new Environment(50, 50, 50, 0, 0); // buffer=0
        // root=50, fungal=50, filling=100.
        // New: intoNutrients = 0. intoBuffer = 100.
        // newNutrients = nutrients(0) + delta(2) + released(0) + syphoned(0) + intoNutrients(0) = 2.
        // newBuffer = buffer(0) + intoBuffer(100) - released(0) - syphoned(0) = 100.
        Environment next = env.next(1, 0, 0, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrientBuffer()).isEqualTo(100);
        assertThat(next.nutrients()).isEqualTo(2);
    }

    @Test
    void nutrientInflowDivertedToNutrientsWhenBufferHigh() {
        Environment env = new Environment(50, 50, 50, 50, 100);
        // rootContribution = 50, fungalContribution = 50. Total filling = 100.
        // Divert 100 into nutrients, 0 into buffer.
        // ReleaseRate = 2 (forced by buffer >= 95). Released = 100 / 2 = 50.
        // newNutrients = nutrients(50) + nutrientDelta(-18) + released(50) + syphoned(0) + diverted(100) = 182, not clamped.
        // newBuffer = nutrientBuffer(100) + divertedIntoBuffer(0) - released(50) - syphoned(0) = 50.
        Environment next = env.next(1, 100, 0, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrientBuffer()).isEqualTo(50);
        assertThat(next.nutrients()).isEqualTo(182);
    }

    @Test
    void bufferDrainsGraduallyWhenHigh() {
        Environment env = new Environment(50, 50, 50, 50, 96);
        // rootContribution = 50, fungalContribution = 50. Total filling = 100.
        // Ratio = (96-50)/50 = 46/50 = 0.92.
        // intoNutrients = (int)(100 * (0.05 + 0.92 * 0.95)) = (int)(100 * 0.924) = 92.
        // intoBuffer = 100 - 92 = 8.
        // ReleaseRate = 2 (due to buffer >= 95).
        // Released = 96 / 2 = 48.
        // newBuffer = nutrientBuffer(96) + intoBuffer(8) - released(48) = 56.
        Environment next = env.next(1, 100, 0, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrientBuffer()).isEqualTo(56);
    }

    @Test
    void lowBufferNutrientInflowIsLow() {
        Environment env = new Environment(50, 50, 50, 50, 10);
        // filling = 50+50 = 100.
        // ReleaseRate = 8.
        // Released = 10 / 8 = 1.
        // intoNutrients = 100 / 4 = 25.
        // nutrientDelta = 2 + 0/2 - 100/5 = -18.
        // newNutrients = 50 - 18 + 1 + 25 = 58.
        Environment next = env.next(1, 100, 0, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrients()).isEqualTo(58);
    }

    @Test
    void lowNutrientsShouldNotForceBufferExhaustion() {
        // Environment with low nutrients(0), low buffer(10), and low filling(10).
        Environment env = new Environment(50, 50, 50, 0, 10);
        // filling = 10.
        // releaseRate = 2.
        // Released = 10 / 2 = 5.
        // intoBuffer = 10 - (10/4 = 2) = 8.
        // newBuffer = 10(buffer) + 8(intoBuffer) - 5(released) = 13.
        // The buffer accumulates.
        Environment next = env.next(1, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrientBuffer()).isGreaterThan(10);
    }
}
