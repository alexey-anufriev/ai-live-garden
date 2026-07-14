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
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", env, 0, 0L, root);
        
        // With nutrients > 75 and buffer > 75, for root network, it should be -15
        assertThat(modifier).isEqualTo(-15);
    }

    @Test
    public void testNutrientUtilizationReducesThresholdForRootNetworkWithMediumNutrientsHighBuffer() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "nutrient-dependent-reproduction");
        Environment env = new Environment(50, 60, 50, 60, 80); // Nutrients 60 (> 50, <= 75), Buffer 80
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("nutrient-dependent-reproduction", env, 0, 0L, root);
        
        // With nutrients > 50 and <= 75, it should be -4
        assertThat(modifier).isEqualTo(-4);
    }

    @Test
    public void testRootNutrientUtilizerContribution() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "root-nutrient-utilizer");
        Environment env = new Environment(30, 60, 50, 60, 80); // Nutrients > 25 (uses else block)
        
        int contribution = TraitRegistry.calculateRootContribution(java.util.List.of(root), env, 0);
        
        // Bonus for root-nutrient-utilizer is 25. Plus base contribution of 1 (rootNetworkCount(1)/2 = 0, + 1 = 1)
        // Math.max(1, 1/2) + 25 = 1 + 25 = 26.
        assertThat(contribution).isEqualTo(26);
    }
}
