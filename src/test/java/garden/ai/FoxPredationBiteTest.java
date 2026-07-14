package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoxPredationBiteTest {

    @Test
    public void testBeetlePredationOptimizerLowDensityPenalty() {
        // Create a fox with the trait
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-predation-optimizer");
        // Create a beetle to be the prey
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 20, 4, "test");
        
        // Create 1000 beetles (low density, < 2000)
        List<Organism> allOrganisms = new ArrayList<>();
        allOrganisms.add(fox);
        allOrganisms.add(beetle);
        IntStream.range(0, 1000).forEach(i -> 
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 1, 1, "test"))
        );
        
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Calculate bite with 1001 beetles (1000 + 1)
        TraitRegistry.BiteEffect biteEffect = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, allOrganisms);
        
        // Base bite for Fox is 3.
        // Beetle-predation-optimizer adds +5 base + 1 density bonus.
        // Penalty for < 2000 is 3.
        // Total bite = 3 + Math.max(1, 5 + 1 - 3) = 3 + 3 = 6.
        assertEquals(6, biteEffect.biteSize(), "Bite size should be 6 with 1001 beetles due to penalty");
    }

    @Test
    public void testBeetlePredationOptimizerIncreasesBiteSize() {
        // Create a fox with the new trait
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-predation-optimizer");
        // Create a beetle to be the prey
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 20, 4, "test");
        
        // Create 2001 beetles (density > 2000, no penalty)
        List<Organism> allOrganisms = new ArrayList<>();
        allOrganisms.add(fox);
        allOrganisms.add(beetle);
        IntStream.range(0, 2000).forEach(i -> 
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 1, 1, "test"))
        );
        
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Calculate bite with 2001 beetles
        TraitRegistry.BiteEffect biteEffect = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, allOrganisms);
        
        // Base bite for Fox is 3.
        // Beetle-predation-optimizer adds +5 base.
        // Beetle count = 2001. Density bonus = Math.min(10, 2001 / 1000) = 2.
        // Total bite = 3 + 5 + 2 = 10.
        assertEquals(10, biteEffect.biteSize(), "Bite size should be 10 with 2001 beetles");
    }

    @Test
    public void testBeetlePredationOptimizerWithHighDensity() {
        // Create a fox with the new trait
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-predation-optimizer");
        // Create a beetle to be the prey
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 20, 4, "test");
        
        // Create 2001 beetles
        List<Organism> allOrganisms = new ArrayList<>();
        allOrganisms.add(fox);
        allOrganisms.add(beetle);
        IntStream.range(0, 2000).forEach(i -> 
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 1, 1, "test"))
        );
        
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Calculate bite with 2001 beetles (2000 + 1)
        TraitRegistry.BiteEffect biteEffect = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, allOrganisms);
        
        // Base bite for Fox is 3.
        // Beetle-predation-optimizer adds +5 base + 2 density bonus.
        // Penalty = 0.
        // Total bite should be 3 + 5 + 2 = 10.
        assertEquals(10, biteEffect.biteSize(), "Bite size should be 10 with 2001 beetles");
    }

    @Test
    public void testBeetlePredationOptimizerAndCoordinatedPredatorSynergy() {
        // Create a fox with both traits
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "beetle-predation-optimizer", "coordinated-predator");
        // Create a beetle to be the prey
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 20, 4, "test");
        
        // Create 2001 beetles
        List<Organism> allOrganisms = new ArrayList<>();
        allOrganisms.add(fox);
        allOrganisms.add(beetle);
        IntStream.range(0, 2000).forEach(i -> 
            allOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 1, 1, "test"))
        );
        
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Calculate bite with 2001 beetles
        TraitRegistry.BiteEffect biteEffect = TraitRegistry.calculateBite(fox, beetle, env, 0, 0, allOrganisms);
        
        // Base bite for Fox is 3.
        // Beetle-predation-optimizer adds +5 base + 2 density bonus.
        // Coordinated-predator adds +5 synergy bonus.
        // Penalty = 0.
        // Total bite = 3 + 5 + 2 + 5 = 15.
        assertEquals(15, biteEffect.biteSize(), "Bite size should be 15 with both traits and 2001 beetles");
    }
}
