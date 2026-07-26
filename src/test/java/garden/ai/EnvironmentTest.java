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
        // ReleaseRate = 1 (forced by buffer >= 95). Released = 100.
        // newNutrients = nutrients(50) + nutrientDelta(-18) + released(100) + syphoned(0) + diverted(100) = 232, clamped to 100.
        // newBuffer = nutrientBuffer(100) + divertedIntoBuffer(0) - released(100) - syphoned(0) = 0.
        Environment next = env.next(1, 100, 0, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrientBuffer()).isEqualTo(0);
        assertThat(next.nutrients()).isEqualTo(100);
    }

    @Test
    void bufferDrainsAggressivelyWhenHigh() {
        Environment env = new Environment(50, 50, 50, 50, 96);
        // rootContribution = 50, fungalContribution = 50. Total filling = 100.
        // Buffer >= 95, so intoNutrients = filling(100), intoBuffer = -filling(-100).
        // ReleaseRate = 10.
        // Released = 96 / 10 = 9.
        // newBuffer = nutrientBuffer(96) + intoBuffer(-100) - released(9) = -13, clamped to 0.
        Environment next = env.next(1, 100, 0, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(next.nutrientBuffer()).isEqualTo(0);
    }

}
