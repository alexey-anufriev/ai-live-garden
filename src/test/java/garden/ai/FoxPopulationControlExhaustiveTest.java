package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;

public class FoxPopulationControlExhaustiveTest {

    @Test
    public void testFoxCullingIsNotRevivedByMetabolism() {
        Environment env = new Environment(50, 50, 50, 100, 100);
        // Create 2500 foxes, > 2000 threshold, with a trait that provides energy
        List<Organism> organisms = IntStream.range(0, 2500)
                .mapToObj(i -> Organism.of("fox-" + i, OrganismType.FOX, 20, 1, "fox-metabolic-efficiency"))
                .collect(Collectors.toList());

        OrganismInteractionCalculator.PassiveChangeContext context = new OrganismInteractionCalculator.PassiveChangeContext(
                env, 1, new ArrayList<>(), new TraitRegistry.ContributionResult(0,0,0,0), organisms);

        List<Organism> changed = OrganismInteractionCalculator.calculatePassiveChanges(context);

        // ALL foxes should have energy 0
        long foxesWithEnergy = changed.stream()
                .filter(o -> o.type() == OrganismType.FOX && o.energy() > 0)
                .count();

        assertThat(foxesWithEnergy).isEqualTo(0);
    }
}
