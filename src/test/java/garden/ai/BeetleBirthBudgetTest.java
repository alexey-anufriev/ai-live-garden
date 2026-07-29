package garden.ai;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

public class BeetleBirthBudgetTest {

    @Test
    public void testBeetleBirthBudgetWithLowBuffer() {
        // Create an environment with low nutrient buffer
        Environment environment = new Environment(50, 50, 50, 50, 0);
        List<Organism> organisms = new ArrayList<>();
        // Add 100 organisms to trigger DENSITY_PRESSURE_MINIMUM_POPULATION
        for (int i = 0; i < 100; i++) {
            organisms.add(Organism.of("organism-" + i, OrganismType.ROOT_NETWORK, 10, 1));
        }
        
        // Add more beetles to trigger the density-dependent restriction
        for (int i = 0; i < 20; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 20, 1));
        }

        int budget = OrganismInteractionCalculator.typeBirthBudget(OrganismType.BEETLE, organisms, environment);
        
        // Currently this returns 0. I want it to be > 0.
        assertTrue(budget > 0, "Beetle birth budget should be > 0 even with low nutrient buffer, currently: " + budget);
    }
}
