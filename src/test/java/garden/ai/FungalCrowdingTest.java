package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class FungalCrowdingTest {
    @Test
    void fungusIsImmuneToCrowdingStress() {
        // Environment favors plants (all > 40)
        Environment env = new Environment(50, 50, 50, 50, 100);
        
        // Setup 5001 MOSS to trigger overcrowding
        List<Organism> organisms = new ArrayList<>();
        for (int i = 0; i < 5001; i++) {
            organisms.add(Organism.of("moss-" + i, OrganismType.MOSS, 10, 1));
        }
        
        // Add one FUNGUS
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        organisms.add(fungus);

        // Verify FUNGUS is not stressed by crowding
        boolean isStressed = TraitRegistry.isPlantStressed(fungus, env, organisms);
        
        // This should be false
        assertThat(isStressed).isFalse();
    }
}
