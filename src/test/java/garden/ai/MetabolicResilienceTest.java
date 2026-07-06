package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class MetabolicResilienceTest {

    @Test
    public void testMetabolicResiliencePreventsStarvation() {
        Environment env = new Environment(10, 10, 10, 0, 0); // Low nutrients
        Organism fox = new Organism("fox-1", OrganismType.FOX, 10, 0, 0, List.of("metabolic-resilience"));
        
        // Should not be starving due to trait
        assertThat(TraitRegistry.isAnimalStarving(fox, env, List.of())).isFalse();
    }

    @Test
    public void testStarvationWithoutResilience() {
        Environment env = new Environment(10, 10, 10, 0, 0); // Low nutrients
        Organism fox = new Organism("fox-1", OrganismType.FOX, 10, 0, 0, List.of());
        
        // Should be starving without trait
        assertThat(TraitRegistry.isAnimalStarving(fox, env, List.of())).isTrue();
    }
}
