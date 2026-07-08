package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class RootSoilEnricherReproductionTest {

    @Test
    public void testRootSoilEnricherReducesThresholdForRootNetworkWithHighBuffer() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "root-soil-enricher");
        Environment env = new Environment(50, 50, 50, 50, 60); // Buffer is 60 (> 40)
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("root-soil-enricher", env, 0, root);
        
        assertThat(modifier).isEqualTo(-4);
    }

    @Test
    public void testRootSoilEnricherDoesNotReduceThresholdForRootNetworkWithLowBuffer() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "root-soil-enricher");
        Environment env = new Environment(50, 50, 50, 50, 30); // Buffer is 30 (< 40)
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("root-soil-enricher", env, 0, root);
        
        assertThat(modifier).isEqualTo(0);
    }

    @Test
    public void testRootSoilEnricherDoesNotReduceThresholdForOtherOrganism() {
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "root-soil-enricher");
        Environment env = new Environment(50, 50, 50, 50, 60); // Buffer is 60
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("root-soil-enricher", env, 0, beetle);
        
        assertThat(modifier).isEqualTo(0);
    }
}
