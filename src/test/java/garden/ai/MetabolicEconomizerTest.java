package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetabolicEconomizerTest {
    @Test
    public void testMetabolicEconomizerReducesMetabolismWhenStressed() {
        // Animal with metabolic-economizer and stressed
        Organism hare = Organism.of("hare-1", OrganismType.HARE, 10, 0, "metabolic-economizer", "stressed");
        // Normal environment
        Garden garden = new Garden(0, 100, new Environment(50, 50, 50, 2, 50), List.of(hare), List.of());

        Garden next = garden.nextCycle();

        Organism nextHare = next.organisms().get(0);

        // Default metabolism for HARE is 1.
        // metabolic-economizer should reduce metabolism by 1 if stressed.
        // New metabolism = 0.
        // Energy = 10 (initial) - 0 (new metabolism) = 10.
        assertEquals(10, nextHare.energy(), "Energy should be 10 (10 - 0)");
    }
}
