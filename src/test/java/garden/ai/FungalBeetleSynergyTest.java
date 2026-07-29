package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FungalBeetleSynergyTest {
    @Test
    void testFungalBeetleSynergy() {
        Environment env = new Environment(100, 50, 50, 100, 100);
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "fungal-beetle-synergizer");
        
        // Fungal contribution > 0: energy bonus = 20 + 10/10 = 21; metabolism -2
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("fungal-beetle-synergizer", 1, beetle, env, 10, 0, 1);
        assertNotNull(effect);
        assertEquals(21, effect.energyBonus());
        assertEquals(-2, effect.metabolismChange());

        // Fungal contribution > 100 to test scaling: 20 + 250/10 = 20 + 25 = 45
        TraitRegistry.MetabolicEffect effectScaled = TraitRegistry.getMetabolicEffect("fungal-beetle-synergizer", 1, beetle, env, 250, 0, 1);
        assertNotNull(effectScaled);
        assertEquals(45, effectScaled.energyBonus());

        // Reproduction threshold modifier: -2
        int modifier = TraitRegistry.getReproductionThresholdModifier("fungal-beetle-synergizer", env, 10, 0, beetle);
        assertEquals(-2, modifier);

        // Fungal contribution == 0
        TraitRegistry.MetabolicEffect effectNoFungus = TraitRegistry.getMetabolicEffect("fungal-beetle-synergizer", 1, beetle, env, 0, 0, 1);
        assertNotNull(effectNoFungus);
        assertEquals(20, effectNoFungus.energyBonus());
        assertEquals(-2, effectNoFungus.metabolismChange());
    }
}
