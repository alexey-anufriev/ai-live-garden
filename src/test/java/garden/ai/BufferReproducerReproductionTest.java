package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BufferReproducerReproductionTest {

    @Test
    public void testBufferReproducerReducesThresholdWhenBufferIsHigh() {
        // High nutrient buffer
        Environment envHighBuffer = new Environment(50, 50, 50, 50, 60); 
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("buffer-reproducer", envHighBuffer, 0);
        
        assertThat(modifier).isEqualTo(-5);
    }

    @Test
    public void testBufferReproducerDoesNotReduceThresholdWhenBufferIsLow() {
        // Low nutrient buffer
        Environment envLowBuffer = new Environment(50, 50, 50, 50, 20); 
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("buffer-reproducer", envLowBuffer, 0);
        
        assertThat(modifier).isEqualTo(0);
    }
}
