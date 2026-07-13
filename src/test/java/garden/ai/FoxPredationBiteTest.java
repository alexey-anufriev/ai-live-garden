package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxPredationBiteTest {

    @Test
    public void testBeetlePredationOptimizerIncreasesBiteSize() {
        // Create a fox with the new trait
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-predation-optimizer");
        // Create a beetle to be the prey
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 20, 4, "test");
        
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Calculate bite
        TraitRegistry.BiteEffect biteEffect = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, List.of(fox, beetle));
        
        // Base bite for Fox is 3.
        // Beetle-predation-optimizer adds +5.
        // Total bite should be 3 + 5 = 8.
        assertEquals(8, biteEffect.biteSize(), "Bite size should be 8 with beetle-predation-optimizer");
    }
}
