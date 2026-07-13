package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmergencyPredatorIntroductionTest {

    private static final Random ALWAYS_COLONIZE = new Random() {
        @Override
        public int nextInt(int bound) {
            return 0;
        }
    };

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        System.setProperty("disable.emergency.colonization", "false");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setProperty("disable.emergency.colonization", "true");
    }

    @Test
    void predatorColonizesWhenHerbivoresHaveNoPredator() {
        List<Organism> organisms = List.of(
                Organism.of("beetle-1", OrganismType.BEETLE, 5, 2, "test"),
                Organism.of("moss-1", OrganismType.MOSS, 10, 1, "test"));
        List<GardenEvent> events = new ArrayList<>();

        OrganismInteractionCalculator.PopulationDynamicsResult result =
                OrganismInteractionCalculator.calculatePopulationDynamics(
                        new OrganismInteractionCalculator.PopulationDynamicsContext(
                                new Environment(50, 50, 50, 50, 50),
                                organisms, 1, 3, events, 0, ALWAYS_COLONIZE));

        assertTrue(result.organisms().stream().anyMatch(o -> o.type() == OrganismType.FOX),
                "No FOX predator introduced");
        assertTrue(events.stream().anyMatch(e -> e.description().contains("colonize")),
                "Colonization event not logged");
    }

    @Test
    void predatorColonizationCanRestoreMoreThanOneFox() {
        List<Organism> organisms = List.of(
                Organism.of("beetle-1", OrganismType.BEETLE, 5, 2, "test"),
                Organism.of("fox-1", OrganismType.FOX, 5, 8, "test"),
                Organism.of("moss-1", OrganismType.MOSS, 50, 1, "test"));

        OrganismInteractionCalculator.PopulationDynamicsResult result =
                OrganismInteractionCalculator.calculatePopulationDynamics(
                        new OrganismInteractionCalculator.PopulationDynamicsContext(
                                new Environment(50, 50, 50, 50, 50),
                                organisms, 1, 4, new ArrayList<>(), 0, ALWAYS_COLONIZE));

        assertTrue(result.organisms().stream().filter(o -> o.type() == OrganismType.FOX).count() >= 2,
                "Multiple FOX predators not introduced");
    }
}
