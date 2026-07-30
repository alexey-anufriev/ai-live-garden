package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NutrientFluctuationTest {
    @Test
    void highNutrientsIncreaseConsumption() {
        // Environment at saturation: 200 nutrients, 200 buffer.
        Environment env = new Environment(50, 50, 50, 200, 200);
        
        // plantCount=500, animalCount=0.
        // Expecting consumption to be high enough to cause nutrients to drop below 200.
        Environment next = env.next(1, 500, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        
        assertThat(next.nutrients()).isLessThan(200);
    }
}
