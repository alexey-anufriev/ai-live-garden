package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

public class RootSoilInteractionTest {

    @Test
    void testRootSoilEnricherHighBufferReproduction() {
        Environment env = new Environment(100, 100, 100, 100, 80); // Buffer 80
        Organism root = new Organism("root-1", OrganismType.ROOT_NETWORK, 0, 0, 0, List.of("root-soil-enricher"));
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("root-soil-enricher", env, 0, root);
        assertEquals(-10, modifier, "Reproduction modifier should be -10 when buffer > 75");

        env = new Environment(100, 100, 100, 100, 50); // Buffer 50
        modifier = TraitRegistry.getReproductionThresholdModifier("root-soil-enricher", env, 0, root);
        assertEquals(-6, modifier, "Reproduction modifier should be -6 when 40 < buffer <= 75");
    }

    @Test
    void testRootSoilEnricherHighBufferMetabolism() {
        Environment env = new Environment(100, 100, 100, 100, 80); // Buffer 80
        Organism root = new Organism("root-1", OrganismType.ROOT_NETWORK, 0, 0, 0, List.of("root-soil-enricher"));
        
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("root-soil-enricher", 1, root, env, 0, 0);
        assertEquals(3, effect.energyBonus(), "Energy bonus should be 3 when buffer > 75");

        env = new Environment(100, 100, 100, 100, 60); // Buffer 60
        effect = TraitRegistry.getMetabolicEffect("root-soil-enricher", 1, root, env, 0, 0);
        assertEquals(1, effect.energyBonus(), "Energy bonus should be 1 when 50 < buffer <= 75");
    }
}
