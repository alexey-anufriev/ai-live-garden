package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

public class RootNetworkPioneerReproductionTest {

    @Test
    public void testPioneerBypassesBirthLimit() {
        // Create 3 root networks, all have pioneer trait.
        // Buffer > 80.
        // All 3 should reproduce even if birthsThisCycle limit is 2.
        
        List<Organism> organisms = new ArrayList<>();
        organisms.add(Organism.of("root-1", OrganismType.ROOT_NETWORK, 100, 1, "nutrient-pioneer"));
        organisms.add(Organism.of("root-2", OrganismType.ROOT_NETWORK, 100, 1, "nutrient-pioneer"));
        organisms.add(Organism.of("root-3", OrganismType.ROOT_NETWORK, 100, 1, "nutrient-pioneer"));
        
        // Environment with nutrient buffer > 80
        Environment env = new Environment(50, 50, 50, 100, 100); 
        
        OrganismInteractionCalculator.PopulationDynamicsContext context = 
            new OrganismInteractionCalculator.PopulationDynamicsContext(env, organisms, 1, 10, new ArrayList<>(), 0, new Random(123));
            
        OrganismInteractionCalculator.PopulationDynamicsResult result = 
            OrganismInteractionCalculator.calculatePopulationDynamics(context);
            
        // We started with 3 organisms, each should have reproduced, so 3 parents + 3 children = 6 organisms total.
        assertThat(result.organisms().size()).isEqualTo(6);
    }
}
