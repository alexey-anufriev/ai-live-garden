package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PredatorEnergyEfficiencyTest {

    @Test
    public void testPredatorEnergyEfficiencyBiteIncrease() {
        Organism hunter = Organism.of("fox-1", OrganismType.FOX, 10, 1, "predator-energy-efficiency");
        Organism prey = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "none");
        Environment environment = new Environment(1, 10, 10, 10, 10);
        List<Organism> allOrganisms = List.of(hunter, prey);

        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(hunter, prey, environment, 1, 0, allOrganisms);

        // Base bite for fox is 3. Predator-energy-efficiency should add 8. Total should be 11.
        assertTrue(result.biteSize() >= 11, "Fox with predator-energy-efficiency should have a bite size of at least 11, but was " + result.biteSize());
        assertTrue(result.events().stream().anyMatch(e -> e.description().contains("predator-energy-efficiency")), "Bite result should contain predator-energy-efficiency event");
    }
}
