package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

public class FungalBufferStabilizerTest {

    @Test
    public void testFungalContributionWithBufferStabilizer() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-buffer-stabilizer");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1), List.of());
        
        // Count: 1 FUNGUS (2 * 2 = 4), 1 buffer-stabilizer (12 bonus) = 16
        assertEquals(16, garden.fungalContribution());
    }
}
