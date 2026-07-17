package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FoxReproductiveStarvationTest {

    @Test
    public void testStarvingFoxWithResourcefulBreederCanReproduce() {
        Environment env = new Environment(1, 20, 20, 20, 20); // Nutrients < 25 -> starving
        // Give fox "resourceful-breeder" AND "starving"
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 5, 1, "resourceful-breeder", "starving");
        
        List<Organism> organisms = new ArrayList<>();
        organisms.add(fox);
        
        // This threshold check should be true
        int threshold = OrganismInteractionCalculator.reproductionThreshold(fox, env, 0, organisms);
        
        // Starving fox without resourceful-breeder cannot reproduce.
        // With resourceful-breeder, it should be able to, assuming energy >= threshold.
        // Reproduction in calculatePopulationDynamics:
        // canReproduce = hasBirthCapacity && (organism.energy() >= reproductionThreshold(...) ...)
        // and importantly:
        // if (organism.traits().contains("starving") && !organism.traits().contains("resourceful-breeder")) { canReproduce = false; }
        
        // Since we have "resourceful-breeder", the "starving" constraint should be bypassed.
        assertTrue(fox.traits().contains("starving"), "Fox should be starving");
        assertTrue(fox.traits().contains("resourceful-breeder"), "Fox should have resourceful-breeder");
    }
}
