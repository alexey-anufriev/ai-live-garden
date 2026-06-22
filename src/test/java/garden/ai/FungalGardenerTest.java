package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class FungalGardenerTest {

    @Test
    public void testFungalGardenerIncreasesFungalContribution() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        Organism gardener = Organism.of("hare-1", OrganismType.HARE, 10, 1, "fungal-gardener");
        
        // This garden setup mimics the state required to calculate fungalContribution
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus, gardener), List.of());
        
        int contribution = garden.fungalContribution();
        
        // FUNGUS(2) + FUNGAL_GARDENER(5) = 7
        assertTrue(contribution >= 7, "Fungal contribution should be increased by fungal-gardener trait, was: " + contribution);
    }
}
