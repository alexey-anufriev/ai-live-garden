package garden.ai;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GardenRecoveryTest {

    @Test
    void populationLimitPreservesMinorityRolesAndCapsTotal() {
        List<Organism> organisms = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 1));
        }
        organisms.add(Organism.of("fox-1", OrganismType.FOX, 10, 1));
        organisms.add(Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1));
        organisms.add(Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1));
        Garden garden = new Garden(10, 200, new Environment(50, 50, 50, 50, 50), organisms, List.of());

        Garden recovered = GardenRecovery.limitPopulation(garden, 20);

        assertThat(recovered.organisms()).hasSize(20);
        assertThat(recovered.organisms()).extracting(Organism::type)
                .contains(OrganismType.FOX, OrganismType.FUNGUS, OrganismType.ROOT_NETWORK);
        assertThat(recovered.events()).anyMatch(event -> event.description().contains("Emergency stewardship"));
        assertThat(recovered.cycle()).isEqualTo(garden.cycle());
        assertThat(recovered.nextId()).isEqualTo(garden.nextId());
    }

    @Test
    void populationWithinLimitIsUnchanged() {
        Garden garden = Garden.seed();

        assertThat(GardenRecovery.limitPopulation(garden, 20)).isSameAs(garden);
    }
}
