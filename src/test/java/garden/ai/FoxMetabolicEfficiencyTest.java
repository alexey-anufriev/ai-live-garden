package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FoxMetabolicEfficiencyTest {

    @Test
    public void testFoxMetabolicEfficiencyHighBuffer() {
        Environment env = new Environment(50, 50, 50, 50, 75); // buffer > 50
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "fox-metabolic-efficiency");
        
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("fox-metabolic-efficiency", 1, fox, env, 0, 0);
        
        assertNotNull(effect);
        assertEquals(-2, effect.metabolismChange());
        assertEquals(8, effect.energyBonus());
        assertTrue(effect.event().description().contains("thrived with high metabolic efficiency"));
    }

    @Test
    public void testFoxMetabolicEfficiencyLowBuffer() {
        Environment env = new Environment(50, 50, 50, 50, 25); // buffer <= 50
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "fox-metabolic-efficiency");
        
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("fox-metabolic-efficiency", 1, fox, env, 0, 0);
        
        assertNotNull(effect);
        assertEquals(-1, effect.metabolismChange());
        assertEquals(4, effect.energyBonus());
        assertTrue(effect.event().description().contains("maintained metabolic efficiency"));
    }
    
    @Test
    public void testNonFoxMetabolicEfficiency() {
        Environment env = new Environment(50, 50, 50, 50, 75);
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fox-metabolic-efficiency");
        
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("fox-metabolic-efficiency", 1, fungus, env, 0, 0);
        
        assertNull(effect);
    }
}
