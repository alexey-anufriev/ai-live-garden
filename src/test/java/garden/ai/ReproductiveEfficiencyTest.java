package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReproductiveEfficiencyTest {

    @Test
    public void testReproductiveEfficiencyThresholdReductionForFox() {
        // Use 50 nutrients so that the `nutrients > 60` modifier in `OrganismInteractionCalculator` does not trigger.
        Environment envHighBuffer = new Environment(50, 50, 50, 50, 100);
        Environment envMidBuffer = new Environment(50, 50, 50, 50, 50);
        Environment envLowBuffer = new Environment(50, 50, 50, 50, 0);
        
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 5, 1, "reproductive-efficiency");
        
        // Base fox threshold: 15.
        // High buffer (>75): 15 - 10 = 5.
        // Mid buffer (>40): 15 - 6 = 9.
        // Low buffer (else): 15 - 3 = 12.
        
        assertEquals(5, OrganismInteractionCalculator.reproductionThreshold(fox, envHighBuffer, 0), "Reproduction threshold for FOX with high buffer (>75) should be 5.");
        assertEquals(9, OrganismInteractionCalculator.reproductionThreshold(fox, envMidBuffer, 0), "Reproduction threshold for FOX with mid buffer (>40) should be 9.");
        assertEquals(12, OrganismInteractionCalculator.reproductionThreshold(fox, envLowBuffer, 0), "Reproduction threshold for FOX with low buffer should be 12.");
    }
}
