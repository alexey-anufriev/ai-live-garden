package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FungalBeetleSynergyTest {
    @Test
    void testFungalBeetleSynergy() {
        Environment env = new Environment(100, 50, 50, 100, 100);
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "fungal-beetle-synergizer");
        
        // Fungal contribution > 0
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("fungal-beetle-synergizer", 1, beetle, env, 10, 0, 1);
        assertNotNull(effect);
        assertEquals(2, effect.energyBonus());

        // Fungal contribution > 100 to test scaling: 2 + 250/100 = 4
        TraitRegistry.MetabolicEffect effectScaled = TraitRegistry.getMetabolicEffect("fungal-beetle-synergizer", 1, beetle, env, 250, 0, 1);
        assertNotNull(effectScaled);
        assertEquals(4, effectScaled.energyBonus());

        // Fungal contribution == 0
        TraitRegistry.MetabolicEffect effectNoFungus = TraitRegistry.getMetabolicEffect("fungal-beetle-synergizer", 1, beetle, env, 0, 0, 1);
        assertNull(effectNoFungus);
        }
        }
