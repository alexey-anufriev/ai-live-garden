package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

public class FungalContributionTest {

    @Test
    public void testFungalContributionWithConnector() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-network-connector");
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1), List.of());
        
        // Count: 1 FUNGUS (2), 1 connector (4) = 6
        assertEquals(6, garden.fungalContribution());
    }
}
