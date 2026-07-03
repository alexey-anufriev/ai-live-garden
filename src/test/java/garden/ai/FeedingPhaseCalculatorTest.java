package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeedingPhaseCalculatorTest {

    @Test
    void herbivoreWithNutrientFinderTraitFeedsMoreEfficiently() {
        // Herbivore with nutrient-finder trait.
        Organism herbivore = Organism.of("herbivore-1", OrganismType.HARE, 10, 2, "nutrient-finder");
        Organism plant = Organism.of("plant-1", OrganismType.MOSS, 10, 1, "food");
        // Environment favorable.
        Environment env = new Environment(50, 50, 50, 50, 50);
        List<Organism> organisms = new ArrayList<>(List.of(herbivore, plant));
        
        FeedingPhaseCalculator.FeedingResult result = FeedingPhaseCalculator.calculate(
                new FeedingPhaseCalculator.FeedingPhaseContext(organisms, env, 1, new ArrayList<>()));

        // Feeding: HARE normally gets 2 energy. With nutrient-finder, should be 2+1=3.
        // The herbivore energy should be updated in the result.
        Organism fedHerbivore = result.organisms().stream()
                .filter(o -> o.id().equals("herbivore-1"))
                .findFirst().get();
        
        // Initial energy 10 + 3 = 13.
        assertThat(fedHerbivore.energy()).isEqualTo(13);
    }
}
