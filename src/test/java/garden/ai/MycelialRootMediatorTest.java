package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MycelialRootMediatorTest {
    @Test
    public void testMycelialRootMediatorIncreasesRootContribution() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "test");
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "test");
        Organism animal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "mycelial-root-mediator");
        
        // g1: Root + Fungus, empty events
        Garden g1 = new Garden(0, 3, env, List.of(root, fungus), List.of());
        // g2: Root + Fungus + Animal (Mediator), empty events
        Garden g2 = new Garden(0, 4, env, List.of(root, fungus, animal), List.of());
        
        assertTrue(g2.rootContribution() > g1.rootContribution(), "Mediator animal should increase root contribution when near fungus");
    }

    @Test
    public void testMycelialRootMediatorRequiresFungus() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "test");
        Organism animal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "mycelial-root-mediator");
        
        // g1: Root only
        Garden g1 = new Garden(0, 2, env, List.of(root), List.of());
        // g2: Root + Animal (Mediator), but NO FUNGUS
        Garden g2 = new Garden(0, 3, env, List.of(root, animal), List.of());
        
        assertTrue(g1.rootContribution() == g2.rootContribution(), "Mediator animal should NOT increase root contribution if no fungus is present");
    }
}
