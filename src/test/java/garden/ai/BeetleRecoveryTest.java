package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeetleRecoveryTest {
    @Test
    public void testBeetleRecoveryTraitBonus() {
        Environment env = new Environment(100, 100, 100, 50, 50);
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 2, "beetle-recovery");
        
        // Pass a beetleCount < 10
        TraitRegistry.MetabolicEffect effect = TraitRegistry.calculateMetabolism(1, beetle, env, 0, 0, 0, 1);
        
        assertTrue(effect.energyBonus() >= 15, "Beetle with beetle-recovery should get significant energy bonus when population is low");
        
        // Pass a beetleCount >= 10
        TraitRegistry.MetabolicEffect effect2 = TraitRegistry.calculateMetabolism(1, beetle, env, 0, 0, 0, 10);
        
        assertTrue(effect2.energyBonus() == 0, "Beetle with beetle-recovery should not get energy bonus when population is 10 or more");
    }
}
