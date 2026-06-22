package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class NutrientRefinerTest {

    @Test
    void testNutrientRefinerBoostsFeedingWhenNotStressed() {
        Organism hunter = Organism.of("hare-1", OrganismType.HARE, 10, 5, "nutrient-refiner");
        Organism prey = Organism.of("moss-1", OrganismType.MOSS, 10, 5);
        
        Garden garden = new Garden(0, 1, new Environment(10, 10, 10, 10, 10), List.of(hunter, prey), List.of());
        Garden nextGarden = garden.nextCycle();
        
        Organism updatedHunter = nextGarden.organisms().stream().filter(o -> o.id().equals("hare-1")).findFirst().orElseThrow();
        
        // HARE metabolism 1, base bite 2.
        // nutrient-refiner (+1) = bite 3.
        // Energy: 10 (initial) - 1 (metabolism) + 3 (feeding) = 12.
        assertEquals(12, updatedHunter.energy(), "Hare should have 12 energy after feeding with nutrient-refiner");
    }

    @Test
    void testNutrientRefinerDoesNotBoostFeedingWhenStressed() {
        Organism hunter = Organism.of("hare-1", OrganismType.HARE, 10, 5, "nutrient-refiner", "stressed");
        Organism prey = Organism.of("moss-1", OrganismType.MOSS, 10, 5);
        
        Garden garden = new Garden(0, 1, new Environment(10, 10, 10, 10, 10), List.of(hunter, prey), List.of());
        Garden nextGarden = garden.nextCycle();
        
        Organism updatedHunter = nextGarden.organisms().stream().filter(o -> o.id().equals("hare-1")).findFirst().orElseThrow();
        
        // HARE metabolism 1, base bite 2.
        // stressed -> no boost.
        // Energy: 10 (initial) - 1 (metabolism) + 2 (feeding) = 11.
        assertEquals(11, updatedHunter.energy(), "Hare should have 11 energy after feeding without nutrient-refiner boost due to stress");
    }
}
