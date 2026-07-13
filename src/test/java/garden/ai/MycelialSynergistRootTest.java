package garden.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MycelialSynergistRootTest {

    @Test
    public void testMycelialSynergizerBoostsRootContribution() {
        Organism root = new Organism("root-1", OrganismType.ROOT_NETWORK, 10, 1, 0, List.of("mycelial-synergizer"));
        Organism fungus = new Organism("fungus-1", OrganismType.FUNGUS, 10, 1, 0, List.of());
        Environment env = new Environment(50, 50, 50, 50, 50);

        List<Organism> organismsWithoutSynergizer = List.of(new Organism("root-2", OrganismType.ROOT_NETWORK, 10, 1, 0, List.of()));
        int contributionWithout = TraitRegistry.calculateRootContribution(organismsWithoutSynergizer, env, 0);

        List<Organism> organismsWithSynergizer = List.of(root);
        int contributionWith = TraitRegistry.calculateRootContribution(organismsWithSynergizer, env, 0);

        assertTrue(contributionWith > contributionWithout, "Root contribution should be boosted by the mycelial-synergizer trait");
    }
}
