package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class MetabolicResilienceStrengthTest {

    @Test
    public void testMetabolicResilienceProvidesEnergyBonus() {
        Environment env = new Environment(10, 10, 10, 0, 0); // Low nutrients
        Organism fox = new Organism("fox-1", OrganismType.FOX, 10, 0, 0, List.of("metabolic-resilience"));
        
        // Check effect
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("metabolic-resilience", 1, fox, env, 0, 0);
        
        assertThat(effect.metabolismChange()).isEqualTo(-2);
        assertThat(effect.energyBonus()).isEqualTo(1);
    }
}
