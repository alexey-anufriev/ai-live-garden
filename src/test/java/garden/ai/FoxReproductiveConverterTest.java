package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FoxReproductiveConverterTest {

    @Test
    public void testFoxReproductiveConverterEffect() {
        Environment environment = new Environment(100, 100, 100, 100, 100);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1, "fox-reproductive-converter");
        
        // Test metabolic effect
        TraitRegistry.MetabolicEffect metabolic = TraitRegistry.getMetabolicEffect("fox-reproductive-converter", 1, fox, environment, 0, 0);
        assertNotNull(metabolic, "Metabolic effect should not be null");
        assertEquals(20, metabolic.energyBonus(), "Fox reproductive converter should provide +20 energy bonus");
        
        // Test reproduction threshold modifier
        int modifier = TraitRegistry.getReproductionThresholdModifier("fox-reproductive-converter", environment, 0, fox);
        assertEquals(-20, modifier, "Fox reproductive converter should reduce reproduction threshold by 20 in high buffer environment");
        
        Environment lowBufferEnvironment = new Environment(100, 100, 100, 100, 10);
        int lowBufferModifier = TraitRegistry.getReproductionThresholdModifier("fox-reproductive-converter", lowBufferEnvironment, 0, fox);
        assertEquals(-10, lowBufferModifier, "Fox reproductive converter should reduce reproduction threshold by 10 in low buffer environment");
    }
}
