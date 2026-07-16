package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

public class BeetleColonizationTraitTest {

    @Test
    public void testEmergencyColonizerBeetleGainsTraitsAndEnergy() {
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 5, 2, "emergency-colonizer");
        Environment env = new Environment(100, 100, 100, 100, 100);
        List<Organism> organisms = new ArrayList<>();
        organisms.add(beetle);
        organisms.add(Organism.of("fern-1", OrganismType.FERN, 10, 1, "quiet"));

        OrganismInteractionCalculator.PassiveChangeContext context = new OrganismInteractionCalculator.PassiveChangeContext(
            env, 1, new ArrayList<>(), new TraitRegistry.ContributionResult(0,0,0,0), organisms
        );

        List<Organism> updatedOrganisms = OrganismInteractionCalculator.calculatePassiveChanges(context);
        
        Organism updatedBeetle = updatedOrganisms.stream().filter(o -> o.id().equals("beetle-1")).findFirst().get();
        
        // Check if traits were added
        assertTrue(updatedBeetle.traits().contains("beetle-recovery"), "Beetle should gain beetle-recovery trait");
        assertTrue(updatedBeetle.traits().contains("prolific"), "Beetle should gain prolific trait");
        assertTrue(updatedBeetle.traits().contains("resourceful-breeder"), "Beetle should gain resourceful-breeder trait");
        
        // Beetle metabolism is 1, so energy should be 5 - 1 = 4 if it didn't eat.
        // If it did eat, energy should be > 4.
        System.out.println("Beetle energy: " + updatedBeetle.energy());
        assertTrue(updatedBeetle.energy() >= 4, "Beetle energy should not be lower than 4");
    }
}
