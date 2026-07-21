package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MycelialConduitTest {

    @Test
    public void testMycelialConduitTraitEnergyBoost() {
        // Create an environment with a fungal network presence
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Organism: Beetle with 'mycelial-conduit'
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 5, "mycelial-conduit");
        
        // Fungal network (to trigger conduit effect)
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        
        // Add 201 beetles so that beetle-recovery trait is not added (total = 202)
        List<Organism> beetles = new ArrayList<>();
        for (int i = 0; i < 201; i++) {
            beetles.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 2));
        }
        
        List<Organism> organisms = new ArrayList<>();
        organisms.add(beetle);
        organisms.add(fungus);
        organisms.addAll(beetles);
        
        Garden garden = new Garden(0, 12, env, organisms, List.of());
        
        // Trigger nextCycle, which includes passiveChange
        Garden nextGarden = garden.nextCycle();
        
        Organism nextBeetle = nextGarden.organisms().stream()
                .filter(o -> o.id().equals("beetle-1"))
                .findFirst()
                .orElseThrow();
        
        // Check if energy increased due to mycelial-conduit (it would normally be 10 - metabolism + growth bonus, 
        // beetle has metabolism 1, no growth bonus, so 10 - 1 = 9)
        // With mycelial-conduit +1, energy should be 10 (9 + 1)
        // PLUS it fed on the fungus (bite 2), so 10 + 2 = 12.
        assertEquals(12, nextBeetle.energy(), "Energy should be 12 after conduit bonus and feeding.");
    }
}
