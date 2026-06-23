package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class FungalFertilizerTest {

    @Test
    public void testFungalFertilizerIncreasesFungalContribution() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        Organism fertilizer = Organism.of("hare-1", OrganismType.HARE, 10, 1, "fungal-fertilizer");
        
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus, fertilizer), List.of());
        
        int contribution = garden.fungalContribution();
        
        // FUNGUS(2) + FUNGAL_FERTILIZER(7) = 9
        assertTrue(contribution >= 9, "Fungal contribution should be increased by fungal-fertilizer trait, was: " + contribution);
    }
}
