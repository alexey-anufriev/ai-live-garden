package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NutrientHarvesterTest {

    @Test
    void nutrientHarvesterIncreasesBiteEnergy() {
        // Setup: A hare with nutrient-harvester and one without.
        Organism harvesterHare = Organism.of("hare-1", OrganismType.HARE, 10, 10, "nutrient-harvester");
        Organism regularHare = Organism.of("hare-2", OrganismType.HARE, 10, 10);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 10);
        
        // Environment needs enough nutrients for them to be happy
        Environment env = new Environment(50, 50, 50, 10, 50);

        // Put them in a garden
        Garden garden = new Garden(1, 2, env, List.of(harvesterHare, regularHare, moss), List.of());
        
        // Advance one cycle (feeding happens in nextCycle)
        Garden next = garden.nextCycle();

        // Check who has fed more
        Organism fedHarvester = next.organisms().stream().filter(o -> o.id().equals("hare-1")).findFirst().orElseThrow();
        Organism fedRegular = next.organisms().stream().filter(o -> o.id().equals("hare-2")).findFirst().orElseThrow();
        
        // Hares usually eat MOSS (2 energy by default). 
        // With nutrient-harvester, it should be 2 + 1 = 3.
        // Need to check the events to confirm it happened.
        boolean harvesterFedMore = fedHarvester.energy() > fedRegular.energy();
        assertTrue(harvesterFedMore, "Harvester hare should have gained more energy than regular hare. Harvester: " + fedHarvester.energy() + ", Regular: " + fedRegular.energy());
        
        // Check for the event
        assertTrue(next.events().stream().anyMatch(e -> e.description().contains("harvested additional nutrients")), "Missing harvesting event log");
    }
}
