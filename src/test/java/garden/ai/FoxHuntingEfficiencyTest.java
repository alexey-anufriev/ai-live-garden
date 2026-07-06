package garden.ai;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FoxHuntingEfficiencyTest {
    @Test
    public void testFoxSpecialistTracksHighestEnergyPrey() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "fox-specialist");
        Organism lowEnergyBeetle = Organism.of("beetle-1", OrganismType.BEETLE, 5, 4, "test");
        Organism highEnergyBeetle = Organism.of("beetle-2", OrganismType.BEETLE, 20, 4, "test");
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Organisms: fox, lowEnergyBeetle, highEnergyBeetle
        List<Organism> organisms = List.of(fox, lowEnergyBeetle, highEnergyBeetle);
        
        Optional<Integer> preyIndex = TraitRegistry.findPreyIndex(organisms, fox, 0, 0, env, new ArrayList<>());
        
        assertTrue(preyIndex.isPresent(), "Fox specialist should find prey");
        assertEquals(2, preyIndex.get(), "Fox specialist should track the high energy beetle (index 2)");
    }

    @Test
    public void testApexPredatorTracksHighestEnergyPrey() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "apex-predator");
        Organism lowEnergyBeetle = Organism.of("beetle-1", OrganismType.BEETLE, 5, 4, "test");
        Organism highEnergyBeetle = Organism.of("beetle-2", OrganismType.BEETLE, 20, 4, "test");
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Organisms: fox, lowEnergyBeetle, highEnergyBeetle
        List<Organism> organisms = List.of(fox, lowEnergyBeetle, highEnergyBeetle);
        
        Optional<Integer> preyIndex = TraitRegistry.findPreyIndex(organisms, fox, 0, 0, env, new ArrayList<>());
        
        assertTrue(preyIndex.isPresent(), "Apex predator should find prey");
        assertEquals(2, preyIndex.get(), "Apex predator should track the high energy beetle (index 2)");
    }
}
