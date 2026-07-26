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
    void nutrientInflowDivertedToNutrientsWhenBufferHigh() {
        Environment env = new Environment(50, 50, 50, 50, 100);
        // rootContribution = 50, fungalContribution = 50. Total filling = 100.
        // Divert 100 into nutrients, 0 into buffer.
        // ReleaseRate = 2 (forced by buffer >= 95). Released = 100 / 2 = 50.
        // newNutrients = nutrients(50) + nutrientDelta(-18) + released(50) + syphoned(0) + diverted(100) = 182, clamped to 100.
        // newBuffer = nutrientBuffer(100) + divertedIntoBuffer(0) - released(50) - syphoned(0) = 50.
        Environment next = env.next(1, 100, 0, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrientBuffer()).isEqualTo(50);
        assertThat(next.nutrients()).isEqualTo(100);
    }

    @Test
    void bufferDrainsGraduallyWhenHigh() {
        Environment env = new Environment(50, 50, 50, 50, 96);
        // rootContribution = 50, fungalContribution = 50. Total filling = 100.
        // Ratio = (96-50)/50 = 46/50 = 0.92.
        // intoNutrients = (int)(100 * 0.92) = 92.
        // intoBuffer = (int)(100 * (1.0-0.92)) = (int)(8) = 8.
        // ReleaseRate = 2 (due to buffer >= 95).
        // Released = 96 / 2 = 48.
        // newBuffer = nutrientBuffer(96) + intoBuffer(8) - released(48) = 56.
        // Truncation in (int) calculations leads to 55.
        Environment next = env.next(1, 100, 0, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrientBuffer()).isEqualTo(55);
    }

    @Test
    void siphonReducesBufferEffectively() {
        // Environment with buffer 100.
        Environment env = new Environment(50, 50, 50, 50, 100);
        // siphonCount = 10. Syphoned = min(100, 10 * 6) = 60.
        // ReleaseRate = 2. Released = 100 / 2 = 50.
        // newBuffer = 100 + intoBuffer(0) - 50 - 60 = -10, clamped to 0.
        Environment next = env.next(1, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10);
        assertThat(next.nutrientBuffer()).isEqualTo(0);
    }

}
