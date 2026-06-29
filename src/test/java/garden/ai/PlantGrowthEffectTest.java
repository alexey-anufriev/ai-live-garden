package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class PlantGrowthEffectTest {
    @Test
    public void testWaterSeekerGrowth() {
        Environment env = new Environment(50, 40, 50, 50, 50); // moisture < 50
        Organism org = Organism.of("test-1", OrganismType.MOSS, 10, 1, "water-seeker");
        TraitRegistry.PlantGrowthEffect effect = TraitRegistry.getPlantGrowthEffect("water-seeker", 0, org, env, List.of(), 0);
        assertNotNull(effect);
        assertEquals(1, effect.growthChange());
    }

    @Test
    public void testResilientGrowth() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism org = Organism.of("test-1", OrganismType.MOSS, 10, 1, "resilient");
        TraitRegistry.PlantGrowthEffect effect = TraitRegistry.getPlantGrowthEffect("resilient", 0, org, env, List.of(), 0);
        assertNotNull(effect);
        assertEquals(-1, effect.growthChange());
    }
    
    @Test
    public void testFungalFeederGrowth() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism org = Organism.of("test-1", OrganismType.MOSS, 10, 1, "fungal-feeder");
        TraitRegistry.PlantGrowthEffect effect = TraitRegistry.getPlantGrowthEffect("fungal-feeder", 0, org, env, List.of(), 1);
        assertNotNull(effect);
        assertEquals(1, effect.growthChange());
    }
}
