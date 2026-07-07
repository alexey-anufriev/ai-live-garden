package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

public class BeetleSpecialistHuntingTest {

    @Test
    void testBeetleSpecialistPrioritizesHighEnergyBeetle() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-specialist");
        Organism beetleLowEnergy = Organism.of("beetle-low", OrganismType.BEETLE, 2, 10);
        Organism beetleHighEnergy = Organism.of("beetle-high", OrganismType.BEETLE, 20, 10);
        Environment env = new Environment(50, 50, 50, 50, 50);
        List<Organism> organisms = new ArrayList<>(List.of(fox, beetleLowEnergy, beetleHighEnergy));
        int hunterIndex = 0; // fox index

        Optional<Integer> targetIndex = TraitRegistry.findPreyIndex(organisms, fox, hunterIndex, 1, env, new ArrayList<>());

        assertThat(targetIndex).isPresent();
        assertThat(organisms.get(targetIndex.get()).id()).isEqualTo("beetle-high");
    }
}
