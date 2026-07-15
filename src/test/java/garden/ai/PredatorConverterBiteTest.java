package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PredatorConverterBiteTest {

    @Test
    public void testPredatorConverterBiteIncrease() {
        Organism hunter = Organism.of("fox-1", OrganismType.FOX, 10, 1, "predator-converter");
        Organism prey = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1, "none");
        Environment environment = new Environment(1, 10, 10, 10, 10);
        java.util.List<Organism> allOrganisms = new java.util.ArrayList<>();
        allOrganisms.add(hunter);
        allOrganisms.add(prey);
        for(int i = 0; i < 500; i++) {
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 1, "none"));
        }

        TraitRegistry.BiteEffect result = TraitRegistry.calculateBite(hunter, prey, environment, 1, 0, allOrganisms);

        // Base bite for fox is 3. Predator-converter should add 6. Total should be at least 9.
        assertTrue(result.biteSize() >= 9, "Fox with predator-converter should have a bite size of at least 9, but was " + result.biteSize());
        assertTrue(result.events().stream().anyMatch(e -> e.description().contains("predator-converter")), "Bite result should contain predator-converter event");
    }
}
