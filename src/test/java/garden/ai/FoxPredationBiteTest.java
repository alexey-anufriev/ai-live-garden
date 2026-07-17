package garden.ai;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FoxPredationBiteTest {
    @Test
    public void testBeetleScarcityBiteReduction() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-predation-optimizer");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 5, 4, "test");
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        List<Organism> allOrganisms = List.of(fox, beetle);
        
        TraitRegistry.BiteEffect biteEffect = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, allOrganisms);
        
        // Base bite 3 + optimizer (5) + density bonus (0) - penalty (3) = 5
        // Wait, base bite FOX is 3.
        // If beetleCount < 100, bite = Math.max(1, bite - 2).
        // Let's check calculateBite again.
        
        System.out.println("Bite size: " + biteEffect.biteSize());
        // For fox-1 (beetleCount=1)
        // Base fox bite = 3.
        // No scarcity check (it's 1 < 100). So bite becomes max(1, 3 - 2) = 1.
        // THEN beetle-predation-optimizer: 5 + density bonus (0) - penalty (3) = 2.
        // So bite = 1 + 2 = 3.
        
        assertEquals(3, biteEffect.biteSize(), "Fox should have reduced bite size in scarcity");
    }
}
