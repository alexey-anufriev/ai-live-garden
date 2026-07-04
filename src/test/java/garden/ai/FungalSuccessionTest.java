package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FungalSuccessionTest {

    private final Environment env = new Environment(50, 50, 50, 10, 50);

    @Test
    public void testRootNetworkProducesFungus() {
        OrganismType rootNetwork = OrganismType.ROOT_NETWORK;
        // Find a cycle/generation where (cycle + generation) % 11 == 0
        int cycle = 11;
        int generation = 0;
        assertEquals(OrganismType.FUNGUS, rootNetwork.offspringType(cycle, generation, env));
    }

    @Test
    public void testRootNetworkDoesNotAlwaysProduceFungus() {
        OrganismType rootNetwork = OrganismType.ROOT_NETWORK;
        // Find a cycle/generation where (cycle + generation) % 11 != 0
        int cycle = 10;
        int generation = 0;
        assertEquals(OrganismType.ROOT_NETWORK, rootNetwork.offspringType(cycle, generation, env));
    }
}
