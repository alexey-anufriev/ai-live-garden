package garden.ai;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeetleEmergencyReproductionTest {

    @Test
    public void testStressedLowDensityBeetleCanReproduce() {
        Environment environment = new Environment(1, 50, 50, 50, 50);
        List<Organism> organisms = new ArrayList<>();
        
        // Add a stressed, starving beetle.
        // It has traits 'stressed' and 'starving'.
        // Population is low (below 100).
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 20, 1, "stressed", "starving");
        organisms.add(beetle);
        
        // Add 10 more beetles to keep count low (< 100)
        for (int i = 2; i <= 10; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 20, 1));
        }

        List<GardenEvent> events = new ArrayList<>();
        OrganismInteractionCalculator.PopulationDynamicsContext context = 
            new OrganismInteractionCalculator.PopulationDynamicsContext(
                environment, organisms, 1, 100, events, 0, new Random()
            );

        // Run reproduction phase logic
        OrganismInteractionCalculator.PopulationDynamicsResult result = 
            OrganismInteractionCalculator.calculatePopulationDynamics(context);

        // Check if the stressed beetle produced offspring
        long beetleCount = result.organisms().stream().filter(o -> o.type() == OrganismType.BEETLE).count();
        assertTrue(beetleCount > 10, "Beetle population should increase even if stressed, due to low density.");
    }
}
