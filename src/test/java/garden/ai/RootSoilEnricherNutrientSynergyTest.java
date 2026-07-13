package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RootSoilEnricherNutrientSynergyTest {
    @Test
    public void testRootSoilEnricherNutrientSynergyBonus() {
        // High nutrient buffer scenario
        Environment env = new Environment(60, 60, 60, 100, 100); 
        Organism root = new Organism("root-1", OrganismType.ROOT_NETWORK, 10, 5, 1, List.of("root-soil-enricher", "nutrient-synthesizer"));
        
        // We need to check both growth effect AND metabolic effect
        TraitRegistry.PlantGrowthEffect growthEffect = TraitRegistry.getPlantGrowthEffect("root-soil-enricher", 1, root, env, List.of(root), 0);
        TraitRegistry.MetabolicEffect metabEffect = TraitRegistry.getMetabolicEffect("root-soil-enricher", 1, root, env, 0, 0);
        
        // As per code, growthEffect gives 2 bonus, metabEffect gives 3 bonus + 2 synergy = 5.
        
        assertTrue(growthEffect.growthChange() > 0);
        assertEquals(5, metabEffect.energyBonus(), "Should have 3 + 2 synergy bonus");
    }
}
