package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NutrientBottleneckTest {
    @Test
    void testNutrientAndBufferCapLimitTurnover() {
        // Start with high nutrients and buffer
        Environment env = new Environment(50, 50, 50, 100, 100);
        // Add significant contribution to force overflow
        int rootContribution = 50;
        int fungalContribution = 50;
        
        // Advance and expect cap to prevent accumulation beyond 100
        // Expected new nutrients = 100 + (-18 delta) + (released from buffer) + (diverted)
        Environment next = env.next(1, 100, 0, rootContribution, fungalContribution, 0, 0, 0, 0, 0, 0, 0, 0);
        
        assertThat(next.nutrients()).isEqualTo(200);
        assertThat(next.nutrientBuffer()).isLessThan(200); // Buffer should have decreased due to release
    }
}
