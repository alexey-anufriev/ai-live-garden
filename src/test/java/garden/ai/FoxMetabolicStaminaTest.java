package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FoxMetabolicStaminaTest {

    @Test
    public void testFoxStamina() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "fox-stamina");
        
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("fox-stamina", 1, fox, env, 0, 0);
        
        assertNotNull(effect);
        assertEquals(-2, effect.metabolismChange());
        assertEquals(2, effect.energyBonus());
        assertTrue(effect.event().description().contains("sustained itself with high fox-stamina"));
    }
    
    @Test
    public void testNonFoxStamina() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fox-stamina");
        
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("fox-stamina", 1, fungus, env, 0, 0);
        
        assertNull(effect);
    }
}
