package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeetleAsexualReproductionTest {
    @Test
    public void testLoneBeetleReproducesAsexually() {
        Environment env = new Environment(100, 100, 100, 100, 100);
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "test");
        List<Organism> organisms = new ArrayList<>(Collections.singletonList(beetle));
        
        OrganismInteractionCalculator.PopulationDynamicsContext context = new OrganismInteractionCalculator.PopulationDynamicsContext(
            env,
            organisms,
            1,
            1,
            new ArrayList<>(),
            0,
            new Random()
        );
        
        OrganismInteractionCalculator.PopulationDynamicsResult result = OrganismInteractionCalculator.calculatePopulationDynamics(context);
        
        // Should have reproduced: 1 parent (half energy) + 1 child
        assertEquals(2, result.organisms().size(), "Lone beetle should reproduce asexually");
        assertEquals(2, result.organisms().stream().filter(o -> o.type() == OrganismType.BEETLE).count(), "Should be 2 beetles");
    }
}
