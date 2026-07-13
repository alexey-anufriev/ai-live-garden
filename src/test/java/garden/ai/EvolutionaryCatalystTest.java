package garden.ai;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EvolutionaryCatalystTest {

    @Test
    void stressedRootNetworkAcquiresFungalSymbiote() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 20, 5, "stressed");
        Environment environment = new Environment(50, 50, 50, 0, 50);
        List<Organism> organisms = List.of(root);
        List<GardenEvent> events = new ArrayList<>();

        List<Organism> changed = OrganismInteractionCalculator.calculatePassiveChanges(
                new OrganismInteractionCalculator.PassiveChangeContext(
                        environment,
                        30,
                        events,
                        TraitRegistry.calculateContribution(organisms, environment),
                        organisms));

        assertTrue(changed.getFirst().traits().contains("fungal-symbiote"),
                "Stressed ROOT_NETWORK should acquire fungal-symbiote; events: " + events.size());
    }
}
