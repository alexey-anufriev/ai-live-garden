package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FungalSymbioteTest {
    @Test
    void testRootNetworkReproduction() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 100, 5, "fungal-symbiote");
        
        // NextCycle will pass cycle 11+1 = 12.
        // We need (12 + 0) % 11 != 0.
        // We need (cycle + gen) % 11 == 0, so if cycle = 12, we need gen = 10 (12+10=22, 22%11=0).
        
        Organism rootGen10 = new Organism("root-1", OrganismType.ROOT_NETWORK, 100, 5, 10, List.of("fungal-symbiote"));
        
        Garden garden = new Garden(11, 2, new Environment(50, 50, 50, 50, 50), List.of(rootGen10), List.of());
        Garden next = garden.nextCycle();
        
        long fungusCount = next.organisms().stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
        assertEquals(1, fungusCount, "Should have produced FUNGUS");
    }
}
