package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxReproductiveResilienceTest {

    @Test
    public void testFoxReproductiveResilienceLowersThreshold() {
        Environment env = new Environment(1, 10, 10, 10, 10); // Low nutrients
        
        // Fox without trait
        Organism foxNormal = Organism.of("fox-normal", OrganismType.FOX, 5, 1);
        
        // Fox with trait
        Organism foxResilient = Organism.of("fox-resilient", OrganismType.FOX, 5, 1, "fox-reproductive-resilience");
        
        List<Organism> organisms = new ArrayList<>();
        organisms.add(foxNormal);
        organisms.add(foxResilient);
        
        int thresholdNormal = OrganismInteractionCalculator.reproductionThreshold(foxNormal, env, 0, organisms);
        int thresholdResilient = OrganismInteractionCalculator.reproductionThreshold(foxResilient, env, 0, organisms);
        
        // Trait should lower threshold by 8 because nutrients (10) < 25
        assertEquals(thresholdNormal - 8, thresholdResilient, "Resilient fox should have a lower threshold");
    }

    @Test
    public void testFoxReproductionThresholdWithHighPopulation() {
        Environment env = new Environment(50, 50, 50, 30, 30); // Mid nutrients/buffer
        
        // Setup 2500 foxes (threshold should increase by 10)
        List<Organism> organisms = new ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            organisms.add(Organism.of("fox-" + i, OrganismType.FOX, 5, 1));
        }
        
        Organism fox = organisms.get(0);
        int threshold = OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms);
        
        // Base threshold for FOX is 15.
        // With > 2000 foxes, it should increase by 10 => 25.
        assertEquals(25, threshold, "Threshold should increase for high fox population");
    }
}
