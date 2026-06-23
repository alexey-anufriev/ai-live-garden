package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FungalRootSymbiontTest {
    @Test
    public void testFungalRootSymbiontIncreasesContribution() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "test");
        Organism symRoot = Organism.of("root-2", OrganismType.ROOT_NETWORK, 10, 1, "fungal-root-symbiont");
        
        Garden g1 = new Garden(0, 2, env, List.of(root), List.of());
        Garden g2 = new Garden(0, 2, env, List.of(symRoot), List.of());
        
        assertTrue(g2.rootContribution() > g1.rootContribution(), "Symbiont root should contribute more nutrients");
    }
}
