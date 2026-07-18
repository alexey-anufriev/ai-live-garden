package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FungalNutrientCyclerTest {

    @Test
    public void testFungalNutrientCyclerIncreasesNutrients() {
        Environment environment = new Environment(1, 100, 100, 100, 100);
        List<Organism> organisms = new ArrayList<>();
        
        // Add 200 fungus to trigger the cycler bonus
        for (int i = 0; i < 200; i++) {
            organisms.add(new Organism("fungus-" + i, OrganismType.FUNGUS, 10, 10, 1, List.of()));
        }
        
        // Add a dead fungus with the trait. OrganismType.FUNGUS has a base nutrient value of 1.
        Organism deadFungus = new Organism("fungus-201", OrganismType.FUNGUS, 0, 10, 1, List.of("fungal-nutrient-cycler"));
        organisms.add(deadFungus);
        
        OrganismInteractionCalculator.FeedingPhaseContext context = new OrganismInteractionCalculator.FeedingPhaseContext(organisms, environment, 1, new ArrayList<>());
        OrganismInteractionCalculator.FeedingResult feedingResult = OrganismInteractionCalculator.calculateFeeding(context);
        
        // Base value (1) + bonus (200 / 100 = 2) = 3
        assertTrue(feedingResult.totalNutrientContribution() >= 3, "Nutrient contribution should be at least 3, was " + feedingResult.totalNutrientContribution());
    }
}
