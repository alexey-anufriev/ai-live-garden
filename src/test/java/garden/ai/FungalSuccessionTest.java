package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FungalSuccessionTest {

    @Test
    public void testRootNetworkProducesFungus() {
        Environment lowNutrients = new Environment(50, 50, 50, 10, 50);
        OrganismType rootNetwork = OrganismType.ROOT_NETWORK;
        assertEquals(OrganismType.FUNGUS, rootNetwork.offspringType(0, 0, lowNutrients));
    }

    @Test
    public void testRootNetworkDoesNotAlwaysProduceFungus() {
        Environment stableNutrients = new Environment(50, 50, 50, 50, 50);
        OrganismType rootNetwork = OrganismType.ROOT_NETWORK;
        assertEquals(OrganismType.ROOT_NETWORK, rootNetwork.offspringType(0, 0, stableNutrients));
    }
}
