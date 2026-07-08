package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class MassDecomposerMetabolicEffectTest {
    @Test
    void testMassDecomposerMetabolicEffect() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 0, "mass-decomposer");
        Environment environment = new Environment(100, 100, 100, 100, 100);
        
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("mass-decomposer", 1, fungus, environment, 0, 0);
        
        assertNotNull(effect);
        assertEquals(3, effect.energyBonus(), "Fungus with mass-decomposer should gain 3 energy");
        assertEquals(0, effect.metabolismChange(), "Metabolism change should be 0");
    }
}
