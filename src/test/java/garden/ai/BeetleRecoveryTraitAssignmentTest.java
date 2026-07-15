package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeetleRecoveryTraitAssignmentTest {
    @Test
    public void testBeetleRecoveryTraitAssignment() {
        Environment env = new Environment(100, 100, 100, 50, 50);
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 2, "initial-trait");
        List<Organism> organisms = List.of(beetle);
        
        // Use OrganismInteractionCalculator.calculatePassiveChanges to simulate one cycle
        TraitRegistry.ContributionResult contribution = new TraitRegistry.ContributionResult(0, 0, 0, 0);
        List<GardenEvent> events = new ArrayList<>();
        OrganismInteractionCalculator.PassiveChangeContext context = new OrganismInteractionCalculator.PassiveChangeContext(env, 1, events, contribution, organisms);
        
        List<Organism> changed = OrganismInteractionCalculator.calculatePassiveChanges(context);
        
        assertTrue(changed.get(0).traits().contains("beetle-recovery"), "Beetle should receive 'beetle-recovery' trait when population is 1");
        assertTrue(changed.get(0).traits().contains("prolific"), "Beetle should receive 'prolific' trait when population is 1");
    }
}
