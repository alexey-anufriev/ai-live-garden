package garden.ai;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

public class FungalReproductionThresholdTest {

    @Test
    void fungalReproductionThresholdLoweredWithHighBuffer() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        List<Organism> manyFungi = new ArrayList<>();
        for (int i = 0; i < 8000; i++) manyFungi.add(Organism.of("fungus-" + i, OrganismType.FUNGUS, 10, 1));
        
        Environment lowBufferEnv = new Environment(50, 50, 50, 50, 10);
        Environment highBufferEnv = new Environment(50, 50, 50, 50, 100);

        int lowBufferThreshold = OrganismInteractionCalculator.reproductionThreshold(fungus, lowBufferEnv, 0, manyFungi);
        int highBufferThreshold = OrganismInteractionCalculator.reproductionThreshold(fungus, highBufferEnv, 0, manyFungi);

        assertThat(highBufferThreshold).isEqualTo(lowBufferThreshold - 4);
        assertThat(highBufferThreshold).isEqualTo(8 - 4 - 1);
    }

    @Test
    void fungalReproductionThresholdLoweredWithLowPopulation() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        Environment env = new Environment(50, 50, 50, 50, 10);
        
        List<Organism> lowPop = new ArrayList<>();
        for (int i = 0; i < 3000; i++) lowPop.add(Organism.of("fungus-" + i, OrganismType.FUNGUS, 10, 1));
        
        int threshold = OrganismInteractionCalculator.reproductionThreshold(fungus, env, 0, lowPop);
        assertThat(threshold).isEqualTo(8 - 3);
    }

    @Test
    void fungalReproductionThresholdBoundaries() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        Environment env = new Environment(50, 50, 50, 50, 10);
        
        List<Organism> pop6999 = new ArrayList<>();
        for (int i = 0; i < 6999; i++) pop6999.add(Organism.of("fungus-" + i, OrganismType.FUNGUS, 10, 1));
        
        int threshold6999 = OrganismInteractionCalculator.reproductionThreshold(fungus, env, 0, pop6999);
        assertThat(threshold6999).isEqualTo(8 - 3);

        List<Organism> pop7000 = new ArrayList<>();
        for (int i = 0; i < 7000; i++) pop7000.add(Organism.of("fungus-" + i, OrganismType.FUNGUS, 10, 1));
        
        int threshold7000 = OrganismInteractionCalculator.reproductionThreshold(fungus, env, 0, pop7000);
        assertThat(threshold7000).isEqualTo(8 - 1);
    }
}
