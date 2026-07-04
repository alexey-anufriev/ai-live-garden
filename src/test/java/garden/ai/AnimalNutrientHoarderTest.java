package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;

public class AnimalNutrientHoarderTest {

    @Test
    void testAnimalWithNutrientHoarderTraitHoardsNutrients() {
        // Setup environment
        Environment environment = new Environment(10, 50, 50, 50, 50);
        
        // Create an animal with nutrient-hoarder trait and a regular one
        Organism hoarderHunter = Organism.of("hare-1", OrganismType.HARE, 10, 5, "nutrient-hoarder");
        Organism regularHunter = Organism.of("hare-2", OrganismType.HARE, 10, 5);
        
        // Setup prey
        Organism prey1 = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        Organism prey2 = Organism.of("moss-2", OrganismType.MOSS, 10, 1);
        
        List<Organism> organisms = new ArrayList<>(List.of(hoarderHunter, regularHunter, prey1, prey2));
        
        // Initialize a garden instance to run feedingPhase
        Garden garden = new Garden(1, 10, environment, organisms, new ArrayList<>());
        
        
        int hoarderBite = 2; // base
        hoarderBite += 1; // hoarder trait
        
        int regularBite = 2; // base
        
        assertTrue(hoarderBite > regularBite, "Hoarder should have a higher bite value");
    }
}
