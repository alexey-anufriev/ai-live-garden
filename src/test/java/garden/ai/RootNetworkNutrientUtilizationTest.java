package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class RootNetworkNutrientUtilizationTest {

    @Test
    public void testNutrientUtilizationReducesThresholdForRootNetworkWithHighBuffer() {
        // Trait "nutrient-dependent-reproduction" is designed for population growth sensitivity
        // and is applicable to root networks based on getMutationTrait in TraitRegistry
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-dependent-reproduction");
        Environment env = new Environment(50, 80, 50, 80, 80); // Nutrients 80 (> 75), Buffer 80 (> 75)
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", env, 0, root);
        
        // With nutrients > 75 and buffer > 75, for root network, it should be -15
        assertThat(modifier).isEqualTo(-15);
    }

    @Test
    public void testNutrientUtilizationReducesThresholdForRootNetworkWithMediumNutrientsHighBuffer() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-dependent-reproduction");
        Environment env = new Environment(50, 60, 50, 60, 80); // Nutrients 60 (> 50, <= 75), Buffer 80
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", env, 0, root);
        
        // With nutrients > 50 and <= 75, it should be -4
        assertThat(modifier).isEqualTo(-4);
    }
}
