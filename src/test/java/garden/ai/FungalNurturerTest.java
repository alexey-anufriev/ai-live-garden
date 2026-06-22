package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FungalNurturerTest {

    @Test
    public void testFungalNurturerReducesReproductionThresholdWhenNearFungi() {
        // Setup: A Fox with 'fungal-nurturer'.
        // Default reproduction threshold for Fox is 18.
        // With 'fungal-nurturer' + fungi, it should reduce to 15.
        // Give it 20 energy, which is enough to reproduce with the trait (15) and without (18).
        // Let's set energy so it's only enough with the trait.
        // If energy = 17, and metabolism = 2, then energy at reproduction phase = 15.
        // 15 >= 15 (reproduction) BUT 15 < 18 (no reproduction).
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 17, 0, "fungal-nurturer");
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 0);
        
        // Fungal contribution will be calculated based on the fungus.
        Garden garden = new Garden(0, 100, new Environment(50, 50, 50, 50, 50), List.of(fox, fungus), List.of());
        
        Garden next = garden.nextCycle();
        
        // If reproduction occurred, there should be 3 organisms (parentFox, childFox, fungus).
        assertEquals(3, next.organisms().size(), "Fox should have reproduced. Organisms: " + next.organisms());
    }
}
