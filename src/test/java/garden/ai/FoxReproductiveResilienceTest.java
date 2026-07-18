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
}
