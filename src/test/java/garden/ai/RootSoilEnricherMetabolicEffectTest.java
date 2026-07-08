package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RootSoilEnricherMetabolicEffectTest {
    @Test
    public void testRootSoilEnricherMetabolicBonus() {
        Environment envHigh = new Environment(100, 100, 100, 100, 60);
        Environment envLow = new Environment(100, 100, 100, 100, 40);
        Organism root = new Organism("root-1", OrganismType.ROOT_NETWORK, 10, 5, 1, List.of("root-soil-enricher"));

        TraitRegistry.MetabolicEffect effectHigh = TraitRegistry.getMetabolicEffect("root-soil-enricher", 1, root, envHigh, 0, 0);
        TraitRegistry.MetabolicEffect effectLow = TraitRegistry.getMetabolicEffect("root-soil-enricher", 1, root, envLow, 0, 0);

        assertNotNull(effectHigh, "Root-soil-enricher should provide a metabolic effect when buffer is high.");
        assertEquals(1, effectHigh.energyBonus(), "Root-soil-enricher should provide an energy bonus of 1 when buffer is high.");
        assertEquals(0, effectHigh.metabolismChange(), "Root-soil-enricher should not reduce metabolism directly.");

        assertNull(effectLow, "Root-soil-enricher should not provide a metabolic effect when buffer is low.");
    }
}
