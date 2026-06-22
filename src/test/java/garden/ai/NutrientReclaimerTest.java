package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class NutrientReclaimerTest {

    @Test
    public void testNutrientReclaimerBoost() {
        // Predator with nutrient-reclaimer
        Organism hunter = Organism.of("fox-1", OrganismType.FOX, 10, 5, "nutrient-reclaimer");
        // Prey with nutrient-storer
        Organism prey = Organism.of("hare-1", OrganismType.HARE, 10, 5, "nutrient-storer");

        List<Organism> organisms = List.of(hunter, prey);
        Garden garden = new Garden(1, 10, new Environment(50, 50, 50, 50, 50), organisms, List.of());

        // Run nextCycle to trigger feeding phase
        Garden nextGarden = garden.nextCycle();

        // Check that the hunter gained more energy than a normal hunt
        // Base bite for fox is 3. With nutrient-reclaimer vs nutrient-storer, should be higher.
        
        Organism updatedHunter = nextGarden.organisms().stream()
                .filter(o -> o.id().equals("fox-1"))
                .findFirst()
                .orElseThrow();
        
        // Expected: Base 3 + trait bonus (let's say 2) = 5. Initial 10 - 2 (metab) + 5 = 13.
        assertTrue(updatedHunter.energy() >= 13, "Predator should have gained more energy with nutrient-reclaimer trait against nutrient-storer prey. Got energy: " + updatedHunter.energy());
        }
        }

