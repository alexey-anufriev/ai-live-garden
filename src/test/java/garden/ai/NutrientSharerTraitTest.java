package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class NutrientSharerTraitTest {

    @Test
    void testNutrientSharerIncreasesNutrientValue() {
        Organism regular = Organism.of("hare-1", OrganismType.HARE, 10, 5);
        Organism sharer = Organism.of("hare-2", OrganismType.HARE, 10, 5, "nutrient-sharer");

        assertTrue(sharer.nutrientValue() > regular.nutrientValue(), "Sharer should have higher nutrient value");
        assertEquals(regular.nutrientValue() + 3, sharer.nutrientValue(), "Sharer should add 3 to nutrient value");
    }

    @Test
void testNutrientSharerReducesMetabolismWhenStarving() {
        // Need a starving animal
        Organism sharer = Organism.of("hare-1", OrganismType.HARE, 10, 5, "nutrient-sharer", "starving");
        // Force starving by setting environment to low nutrients
        Garden garden = new Garden(0, 1, new Environment(10, 10, 10, 10, 10), List.of(sharer), List.of());
        
        // Advance cycle. 
        Garden nextGarden = garden.nextCycle();
        
        Organism updatedHare = nextGarden.organisms().get(0);
        assertTrue(updatedHare.traits().contains("starving"), "Hare should be starving");
        
        // With nutrient-sharer, metabolism for hare (1) should be 0 (1-2, clamped to 0)
        // Energy should be: 10 - 0 = 10
        assertEquals(10, updatedHare.energy(), "Hare energy should not decrease due to 0 metabolism");
    }
}
