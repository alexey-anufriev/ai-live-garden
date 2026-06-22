package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BufferExplorerTest {
    @Test
    public void testBufferExplorerReducesMetabolismAndBoostsEnergy() {
        // Animal with buffer-explorer and buffer-tapper
        Organism hare = Organism.of("hare-1", OrganismType.HARE, 10, 0, "buffer-explorer", "buffer-tapper");
        // Low nutrients (2), Buffer > 0 (50)
        Garden garden = new Garden(0, 100, new Environment(50, 50, 50, 2, 50), List.of(hare), List.of());
        
        Garden next = garden.nextCycle();
        
        Organism nextHare = next.organisms().get(0);
        
        // Metabolism for Hare is 1.
        // buffer-explorer reduces metabolism by 1 -> 0.
        // Energy = 10 (initial) - 0 (metabolism) = 10.
        // buffer-explorer + buffer-tapper boosts energy by 1 (new) + 1 (original) = 2.
        // Final energy = 10 + 2 = 12.
        assertEquals(12, nextHare.energy(), "Energy should be 12 (10 - 0 + 2)");
    }
}
