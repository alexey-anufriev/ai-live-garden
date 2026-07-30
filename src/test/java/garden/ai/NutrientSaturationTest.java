package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NutrientSaturationTest {

    @Test
    void consumptionIncreasesMoreUnderDoubleSaturation() {
        // Environment with nutrients=195, buffer=195, plantCount=300
        Environment env = new Environment(50, 50, 50, 195, 195);
        
        // Expected with new logic:
        //   consumption = (300/5) + 20 + 20 = 60 + 20 + 20 = 100.
        //   nutrientDelta = 2 + 0 - 100 = -98.
        //   Released = 195 / 2 = 97.
        //   newNutrients = 195 - 98 + 97 = 194.
        
        // With current logic (if I hadn't applied the change):
        //   consumption = 60 + 20 = 80.
        //   nutrientDelta = 2 + 0 - 80 = -78.
        //   newNutrients = 195 - 78 + 97 = 214 -> 200.
        
        Environment next = env.next(1, 300, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        
        assertThat(next.nutrients()).isEqualTo(194);
    }
}
