package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MycelialBufferAdapterTest {
    @Test
    public void testMycelialBufferAdapterReducesMetabolismAndBoostsEnergy() {
        // Animal with mycelial-buffer-adapter (using a Fox to avoid eating Fungus)
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 0, "mycelial-buffer-adapter");
        
        // Fungus providing contribution (fungalContribution > 0)
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 0, "fungal-network");
        
        // Low nutrients (2), Buffer > 0 (50)
        Garden garden = new Garden(0, 100, new Environment(50, 50, 50, 2, 50), List.of(fox, fungus), List.of());
        
        Garden next = garden.nextCycle();
        
        Organism nextFox = next.organisms().stream().filter(o -> o.id().equals("fox-1")).findFirst().orElseThrow();
        
        // Metabolism for Fox is 2.
        // mycelial-buffer-adapter reduces metabolism by 1 -> 1.
        // Energy = 10 (initial) - 1 (metabolism) + 1 (energy boost) = 10.
        assertEquals(10, nextFox.energy(), "Energy should be 10 (10 - 1 + 1)");
    }
}
