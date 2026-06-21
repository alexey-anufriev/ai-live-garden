package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NutrientStorerTraitTest {

    @Test
    void testNutrientStorerTraitIncreasesNutrientValue() {
        Organism regular = Organism.of("hare-1", OrganismType.HARE, 10, 5);
        Organism storer = Organism.of("hare-2", OrganismType.HARE, 10, 5, "nutrient-storer");

        assertTrue(storer.nutrientValue() > regular.nutrientValue(), "Storer should have higher nutrient value");
        assertEquals(regular.nutrientValue() + 6, storer.nutrientValue(), "Storer should add 6 to nutrient value");
    }
}
