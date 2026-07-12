package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.assertj.core.api.Assertions.assertThat;

class PopulationDynamicsTest {

    @Test
    void testReproductionLimitRemoved() {
        Environment env = new Environment(100, 100, 100, 100, 100);
        // Create 5 organisms that should be able to reproduce
        List<Organism> organisms = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            organisms.add(Organism.of("moss-" + i, OrganismType.MOSS, 50, 1));
        }
        
        OrganismInteractionCalculator.PopulationDynamicsContext context = new OrganismInteractionCalculator.PopulationDynamicsContext(
                env, organisms, 1, 10, new ArrayList<>(), 0, new Random());
        
        OrganismInteractionCalculator.PopulationDynamicsResult result = OrganismInteractionCalculator.calculatePopulationDynamics(context);
        
        // With limit (2), total should be 5 + 2 = 7. Without limit, it should be 5 + 5 = 10.
        assertThat(result.organisms().size()).isEqualTo(10);
    }
}
