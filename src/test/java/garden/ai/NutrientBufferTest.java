package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NutrientBufferTest {
    @Test
    void testBufferAccumulation() {
        // Environment(light, moisture, warmth, nutrients, nutrientBuffer)
        Environment env = new Environment(50, 50, 50, 50, 0);
        // next(cycle, plantCount, animalCount, rootContribution, fungalContribution, plantConsumptionReduction, rootConsumptionReduction, mobilizerCount, releaserCount, acceleratorCount, recyclerCount, distributorCount, siphonCount)
        Environment nextEnv = env.next(1, 100, 10, 5000, 10000, 0, 0, 0, 0, 0, 0, 0, 0);
        
        System.out.println("Buffer: " + nextEnv.nutrientBuffer());
        assertTrue(nextEnv.nutrientBuffer() > 0, "Buffer should have accumulated");
    }
}
