package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxPredationBiteTest {

    @Test
    public void testBeetlePredationOptimizerIncreasesBiteSize() {
        // Create a fox with the new trait
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-predation-optimizer");
        // Create a beetle to be the prey
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 20, 4, "test");
        
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Calculate bite with 1 beetle
        TraitRegistry.BiteEffect biteEffect = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, List.of(fox, beetle));
        
        // Base bite for Fox is 3.
        // Beetle-predation-optimizer adds +5 base.
        // Beetle count = 1. Density bonus = 0.
        // Total bite should be 3 + 5 + 0 = 8.
        assertEquals(8, biteEffect.biteSize(), "Bite size should be 8 with 1 beetle");
    }

    @Test
    public void testBeetlePredationOptimizerWithHighDensity() {
        // Create a fox with the new trait
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-predation-optimizer");
        // Create a beetle to be the prey
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 20, 4, "test");
        
        // Create 1500 beetles
        List<Organism> allOrganisms = new ArrayList<>();
        allOrganisms.add(fox);
        allOrganisms.add(beetle);
        IntStream.range(0, 1500).forEach(i -> 
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 1, 1, "test"))
        );
        
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Calculate bite with 1501 beetles (1500 + 1)
        TraitRegistry.BiteEffect biteEffect = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, allOrganisms);
        
        // Base bite for Fox is 3.
        // Beetle-predation-optimizer adds +5 base + (1501 / 1000 = 1) density bonus.
        // Total bite should be 3 + 5 + 1 = 9.
        assertEquals(9, biteEffect.biteSize(), "Bite size should be 9 with 1501 beetles");
    }

    @Test
    public void testBeetlePredationOptimizerAndCoordinatedPredatorSynergy() {
        // Create a fox with both traits
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-predation-optimizer", "coordinated-predator");
        // Create a beetle to be the prey
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 20, 4, "test");
        
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Calculate bite with 1 beetle
        TraitRegistry.BiteEffect biteEffect = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, List.of(fox, beetle));
        
        // Base bite for Fox is 3.
        // Beetle-predation-optimizer adds +5 base + 0 density bonus.
        // Coordinated-predator adds +5 synergy bonus.
        // Total bite should be 3 + 5 + 0 + 5 = 13.
        assertEquals(13, biteEffect.biteSize(), "Bite size should be 13 with both traits");
    }
}
