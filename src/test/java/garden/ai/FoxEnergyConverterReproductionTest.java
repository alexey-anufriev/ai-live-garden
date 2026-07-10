package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxEnergyConverterReproductionTest {

    @Test
    public void testFoxEnergyConverterThresholdReduction() {
        Environment envHighBuffer = new Environment(50, 50, 50, 50, 100);
        Environment envLowBuffer = new Environment(50, 50, 50, 50, 40);
        
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 5, 1, "fox-energy-converter");
        
        // Base fox threshold: 15.
        // High buffer (>75): 15 - 20 = -5.
        // Low buffer (else): 15 - 10 = 5.
        
        assertEquals(-5, OrganismInteractionCalculator.reproductionThreshold(fox, envHighBuffer, 0), "Reproduction threshold for FOX with fox-energy-converter and high buffer (>75) should be -5.");
        assertEquals(5, OrganismInteractionCalculator.reproductionThreshold(fox, envLowBuffer, 0), "Reproduction threshold for FOX with fox-energy-converter and low buffer should be 5.");
    }
}
