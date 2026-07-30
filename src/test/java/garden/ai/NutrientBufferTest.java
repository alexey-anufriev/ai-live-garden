package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NutrientBufferTest {

    @Test
    public void testBufferFillingLogic() {
        // Current state:
        // nutrients = 200 (clamped from 1631)
        // nutrientBuffer = 0
        // filling = 58000
        // intoNutrients = filling / 20 = 2900
        // intoBuffer = filling - 2900 = 55100
        
        Environment env = new Environment(50, 50, 50, 200, 0);
        int filling = 58000;
        
        // This is the logic currently in Environment.next()
        int intoNutrients = env.nutrientBuffer() < 10 ? filling / 4 : (env.nutrientBuffer() < 50 ? filling / 4 : filling / 20);
        
        assertEquals(14500, intoNutrients, "Expected 14500 to go into nutrients");
    }
}
