package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeetlePredationScarcityTest {

    @Test
    public void testFoxPredationEfficiencyIsReducedWhenBeetleCountIsLow() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 8, "test");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 2, "test");
        
        List<Organism> manyBeetles = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            manyBeetles.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 2, "test"));
        }
        
        List<Organism> fewBeetles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            fewBeetles.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 2, "test"));
        }
        
        // Scenario 1: High beetle count
        List<Organism> highBeetleGarden = new ArrayList<>(manyBeetles);
        highBeetleGarden.add(fox);
        
        TraitRegistry.BiteEffect biteHigh = TraitRegistry.calculateBite(fox, beetle, new Environment(50, 50, 50, 50, 50), 0, 0, highBeetleGarden);
        
        // Scenario 2: Low beetle count
        List<Organism> lowBeetleGarden = new ArrayList<>(fewBeetles);
        lowBeetleGarden.add(fox);
        
        TraitRegistry.BiteEffect biteLow = TraitRegistry.calculateBite(fox, beetle, new Environment(50, 50, 50, 50, 50), 0, 0, lowBeetleGarden);
        
        assertTrue(biteLow.biteSize() < biteHigh.biteSize(), 
            "Bite size should be smaller when beetle count is low (Low: " + biteLow.biteSize() + ", High: " + biteHigh.biteSize() + ")");
    }
}
