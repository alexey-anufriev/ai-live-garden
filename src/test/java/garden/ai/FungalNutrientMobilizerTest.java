package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FungalNutrientMobilizerTest {

    @Test
    public void testFungalNutrientMobilizerIncreasesNutrientRelease() {
        Environment initialEnv = new Environment(50, 50, 50, 2, 50);
        Organism fungusWithTrait = Organism.of("fungus-1", OrganismType.FUNGUS, 5, 5, "fungal-nutrient-mobilizer");
        
        Garden garden = new Garden(0, 2, initialEnv, List.of(fungusWithTrait), List.of());
        
        // Advance one cycle
        Garden nextGarden = garden.nextCycle();
        
        // Buffer was 50. With mobilizers (1), releaseRate was base 2 - 1 = 1.
        // Release = 50 / 1 = 50.
        // Nutrients should have increased.
        assertTrue(nextGarden.environment().nutrients() > initialEnv.nutrients(), 
            "Fungal nutrient mobilizer should increase nutrient release, initial=" + initialEnv.nutrients() + ", next=" + nextGarden.environment().nutrients());
    }
}
