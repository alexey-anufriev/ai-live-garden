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

    @Test
    public void testNewBeetleGetsRecoveryTraitsAtBirth() {
        Environment env = new Environment(100, 100, 100, 50, 50);
        // Create a parent beetle with high energy so it can reproduce
        Organism parent = Organism.of("beetle-parent", OrganismType.BEETLE, 20, 2, "initial-trait");
        List<Organism> organisms = List.of(parent);
        
        List<GardenEvent> events = new ArrayList<>();
        OrganismInteractionCalculator.PopulationDynamicsContext context = new OrganismInteractionCalculator.PopulationDynamicsContext(
                env, organisms, 1, 2, events, 0, new java.util.Random(42));
        
        OrganismInteractionCalculator.PopulationDynamicsResult result = OrganismInteractionCalculator.calculatePopulationDynamics(context);
        
        // Find the child beetle
        Organism child = result.organisms().stream()
                .filter(o -> o.type() == OrganismType.BEETLE)
                .filter(o -> !o.id().equals("beetle-parent"))
                .findFirst()
                .orElseThrow();
        
        assertTrue(child.traits().contains("beetle-recovery"), "New beetle should receive 'beetle-recovery' trait at birth when population is 1");
    }
}
