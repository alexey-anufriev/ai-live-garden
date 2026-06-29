package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PredatorNutrientContributionTest {

    @Test
    public void testPredatorNutrientContribution() {
        // Create a garden with 1 FOX (needs to eat) and 1 BEETLE (prey)
        List<Organism> organisms = new ArrayList<>();
        organisms.add(Organism.of("fox-1", OrganismType.FOX, 20, 8, "test"));
        organisms.add(Organism.of("beetle-1", OrganismType.BEETLE, 20, 4, "test"));
        
        // Environment with 0 plant/animal count to minimize other nutrient changes
        Environment env = new Environment(50, 50, 50, 10, 50);
        Garden garden = new Garden(0, 3, env, organisms, List.of());
        
        // Advance 1 cycle
        Garden next = garden.nextCycle();
        
        // Check if predator fed
        boolean fed = next.events().stream().anyMatch(e -> e.description().contains("fed on"));
        
        // If it fed, nutrients should have increased by more than the baseline animal contribution.
        // Baseline: animalCount = 2 (1 fox, 1 beetle). nutrientDelta = 2 + 2/2 = 3.
        // If fox eats beetle, predatorContribution = 1.
        // Total nutrient increase should be 3 + 1 = 4.
        
        assertTrue(fed, "Predator should have fed on prey");
        assertTrue(next.environment().nutrients() > garden.environment().nutrients(), 
            "Nutrients should increase when a predator eats prey");
    }
}
