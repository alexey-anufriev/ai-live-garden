package garden.ai;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MycelialBufferAdapterTest {
    @Test
    void mycelialBufferAdapterOffsetsFoxMetabolismWithFungalContribution() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 0, "mycelial-buffer-adapter");
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 0, "fungal-network");
        Environment environment = new Environment(50, 50, 50, 2, 50);
        List<Organism> organisms = List.of(fox, fungus);

        List<Organism> changed = OrganismInteractionCalculator.calculatePassiveChanges(
                new OrganismInteractionCalculator.PassiveChangeContext(
                        environment,
                        1,
                        new ArrayList<>(),
                        TraitRegistry.calculateContribution(organisms, environment),
                        organisms));
        Organism changedFox = changed.stream()
                .filter(organism -> organism.id().equals("fox-1"))
                .findFirst()
                .orElseThrow();

        // Metabolism for Fox is 2.
        // mycelial-buffer-adapter reduces metabolism by 1 -> 1.
        // Energy = 10 (initial) - 1 (metabolism) + 1 (energy boost) = 10.
        assertEquals(10, changedFox.energy(), "Energy should be 10 (10 - 1 + 1)");
    }
}
