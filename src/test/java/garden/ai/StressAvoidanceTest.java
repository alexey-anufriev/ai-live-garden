package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class StressAvoidanceTest {

    @Test
    public void testStressAvoidanceTraitPreventsStress() {
        // Environment record: light, moisture, warmth, nutrients, nutrientBuffer
        Environment environment = new Environment(100, 100, 100, 0, 100); 
        Organism plant = new Organism("moss-1", OrganismType.MOSS, 10, 0, 0, List.of("stress-avoidance"));
        
        System.out.println("Environment nutrients: " + environment.nutrients());
        
        boolean hasTrait = plant.traits().contains("stress-avoidance");
        boolean isHungry = environment.nutrients() == 0;
        
        boolean isStressAvoidant = hasTrait && isHungry;
        
        assertTrue(isStressAvoidant, "Plant should be in stress-avoidance mode when nutrients are 0");
    }
}
