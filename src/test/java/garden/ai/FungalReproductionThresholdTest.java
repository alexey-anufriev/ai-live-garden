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
        for (int i = 0; i < 6000; i++) manyFungi.add(Organism.of("fungus-" + i, OrganismType.FUNGUS, 10, 1));
        
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
        
        List<Organism> pop4999 = new ArrayList<>();
        for (int i = 0; i < 4999; i++) pop4999.add(Organism.of("fungus-" + i, OrganismType.FUNGUS, 10, 1));
        
        int threshold4999 = OrganismInteractionCalculator.reproductionThreshold(fungus, env, 0, pop4999);
        assertThat(threshold4999).isEqualTo(8 - 3);

        List<Organism> pop5000 = new ArrayList<>();
        for (int i = 0; i < 5000; i++) pop5000.add(Organism.of("fungus-" + i, OrganismType.FUNGUS, 10, 1));
        
        int threshold5000 = OrganismInteractionCalculator.reproductionThreshold(fungus, env, 0, pop5000);
        assertThat(threshold5000).isEqualTo(8 - 1);
    }
}
