package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

public class BufferSiphonTest {

    @Test
    public void testBufferSiphonMovesNutrients() {
        // Setup: Buffer has nutrients, pool has none, one organism with siphon
        Environment env = new Environment(50, 50, 50, 0, 100);
        Organism siphoner = Organism.of("root-siphon", OrganismType.ROOT_NETWORK, 10, 5, "buffer-siphon");
        
        // Advance cycle: siphonCount = 1
        Environment nextEnv = env.next(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1);
        
        // Siphon moves 5 nutrients from buffer (100 -> 95) to pool (0 -> 5)
        assertTrue(nextEnv.nutrientBuffer() <= 95, "Buffer should decrease by at least 5 due to siphon");
        assertTrue(nextEnv.nutrients() >= 5, "Pool should increase by at least 5 due to siphon");
    }
}
