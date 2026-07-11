package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BufferReproducerReproductionTest {

    @Test
    public void testBufferReproducerReducesThresholdWhenBufferIsHigh() {
        // High nutrient buffer
        Environment envHighBuffer = new Environment(50, 50, 50, 50, 60); 
        Organism dummy = Organism.of("dummy", OrganismType.ROOT_NETWORK, 10, 1, "none");
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("buffer-reproducer", envHighBuffer, 0, 0L, dummy);
        
        assertThat(modifier).isEqualTo(-5);
    }

    @Test
    public void testBufferReproducerDoesNotReduceThresholdWhenBufferIsLow() {
        // Low nutrient buffer
        Environment envLowBuffer = new Environment(50, 50, 50, 50, 20); 
        Organism dummy = Organism.of("dummy", OrganismType.ROOT_NETWORK, 10, 1, "none");
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("buffer-reproducer", envLowBuffer, 0, 0L, dummy);
        
        assertThat(modifier).isEqualTo(0);
    }
}
