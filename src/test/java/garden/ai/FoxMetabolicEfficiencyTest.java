package garden.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FoxMetabolicEfficiencyTest {

    @Test
    public void testFoxMetabolicEfficiencyGainsEnergyWhenBeetlesAreScarce() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 0, "fox-metabolic-efficiency");
        Environment env = new Environment(50, 50, 50, 50, 50); // Nutrients, buffer, moisture, warmth, light
        long beetleCount = 5; // Scarce

        TraitRegistry.MetabolicEffect result = TraitRegistry.calculateMetabolism(1, fox, env, 0, 0, 0, beetleCount);
        
        // metabolism = 2 (default FOX).
        // With fox-metabolic-efficiency + low beetles:
        // metabolism should be 0.
        // energyBonus should be 6 (4 + 2).
        
        assertTrue(result.energyBonus() > 0, "Fox should gain energy bonus.");
        assertTrue(result.metabolismChange() == 0, "Fox should have 0 metabolism.");
        
        int netEffect = result.energyBonus() - result.metabolismChange();
        // net gain is 6 - 0 = 6.
        assertTrue(netEffect > 0, "Fox should have a net energy gain.");
    }
}
