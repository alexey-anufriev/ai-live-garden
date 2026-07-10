package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;

public class FungalEnergyConverterTest {

    @Test
    public void testFungalEnergyConverterEffect() {
        Environment environment = new Environment(100, 100, 100, 100, 100);
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-energy-converter");
        
        // Test metabolic effect
        TraitRegistry.MetabolicEffect metabolic = TraitRegistry.getMetabolicEffect("fungal-energy-converter", 1, fungus, environment, 0, 0);
        assertNotNull(metabolic, "Metabolic effect should not be null");
        assertEquals(20, metabolic.energyBonus(), "Fungal energy converter should provide +20 energy bonus");
        
        // Test reproduction threshold modifier
        int modifier = TraitRegistry.getReproductionThresholdModifier("fungal-energy-converter", environment, 0, fungus);
        assertEquals(-20, modifier, "Fungal energy converter should reduce reproduction threshold by 20 in high buffer environment");
        
        Environment lowBufferEnvironment = new Environment(100, 100, 100, 100, 10);
        int lowBufferModifier = TraitRegistry.getReproductionThresholdModifier("fungal-energy-converter", lowBufferEnvironment, 0, fungus);
        assertEquals(-10, lowBufferModifier, "Fungal energy converter should reduce reproduction threshold by 10 in low buffer environment");
    }
}
