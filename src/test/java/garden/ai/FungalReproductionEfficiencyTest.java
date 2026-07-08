package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Set;

public class FungalReproductionEfficiencyTest {

    @Test
    public void testFungalDecomposerAcceleratorReducesThresholdForFungus() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-decomposer-accelerator");
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("fungal-decomposer-accelerator", env, 10, fungus);
        
        assertThat(modifier).isEqualTo(-4);
    }

    @Test
    public void testFungalNutrientAmplifierReducesThresholdForFungus() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-nutrient-amplifier");
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("fungal-nutrient-amplifier", env, 10, fungus);
        
        assertThat(modifier).isEqualTo(-3);
    }

    @Test
    public void testFungalDecomposerAcceleratorDoesNotReduceThresholdForBeetle() {
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "fungal-decomposer-accelerator");
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        int modifier = TraitRegistry.getReproductionThresholdModifier("fungal-decomposer-accelerator", env, 10, beetle);
        
        assertThat(modifier).isEqualTo(0);
    }
}
