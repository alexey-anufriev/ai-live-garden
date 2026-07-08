package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class FungalAttractorTest {

    @Test
    public void testFungalAttractorBoostsAnimalEnergy() {
        // Setup: Root network with fungal-attractor, a fungus, and an animal
        List<Organism> organisms = List.of(
                Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "fungal-attractor"),
                Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "nutrient-decomposer"),
                Organism.of("hare-1", OrganismType.HARE, 10, 1, "glass-footed")
        );
        Garden garden = new Garden(0, 4, new Environment(50, 50, 50, 50, 50), organisms, List.of());

        Garden next = garden.nextCycle();

        Organism hare = next.organisms().stream().filter(o -> o.type() == OrganismType.HARE).findFirst().orElseThrow();

        assertThat(hare.energy()).isEqualTo(12);
    }

    @Test
    public void testNoBoostWithoutFungus() {
        // Setup: Root network with fungal-attractor, NO fungus, and an animal
        List<Organism> organisms = List.of(
                Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "fungal-attractor"),
                Organism.of("hare-1", OrganismType.HARE, 10, 1, "glass-footed")
        );
        Garden garden = new Garden(0, 3, new Environment(50, 50, 50, 50, 50), organisms, List.of());

        Garden next = garden.nextCycle();

        Organism hare = next.organisms().stream().filter(o -> o.type() == OrganismType.HARE).findFirst().orElseThrow();

        assertThat(hare.energy()).isEqualTo(12);
    }
}
