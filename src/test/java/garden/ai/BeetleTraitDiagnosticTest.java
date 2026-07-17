package garden.ai;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;

public class BeetleTraitDiagnosticTest {

    @Test
    public void testBeetleTraitActivation() {
        Environment environment = new Environment(1, 50, 50, 50, 50);
        List<Organism> organisms = new ArrayList<>();
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 5, 1, "test");
        organisms.add(beetle);

        OrganismInteractionCalculator.PassiveChangeContext context = new OrganismInteractionCalculator.PassiveChangeContext(
            environment, 1, new ArrayList<>(), new TraitRegistry.ContributionResult(0, 0, 0, 0), organisms);
        
        List<Organism> changedOrganisms = OrganismInteractionCalculator.calculatePassiveChanges(context);
        Organism changedBeetle = changedOrganisms.get(0);

        assertTrue(changedBeetle.traits().contains("beetle-recovery"), "Beetle should have beetle-recovery trait");
        assertTrue(changedBeetle.traits().contains("prolific"), "Beetle should have prolific trait");
        assertTrue(changedBeetle.traits().contains("resourceful-breeder"), "Beetle should have resourceful-breeder trait");
    }
}
