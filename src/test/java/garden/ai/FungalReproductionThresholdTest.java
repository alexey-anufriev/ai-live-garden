package garden.ai;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class FungalReproductionThresholdTest {

    @Test
    void fungalReproductionThresholdLoweredWithHighBuffer() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        Environment lowBufferEnv = new Environment(50, 50, 50, 50, 10);
        Environment highBufferEnv = new Environment(50, 50, 50, 50, 100);

        int lowBufferThreshold = OrganismInteractionCalculator.reproductionThreshold(fungus, lowBufferEnv, 0);
        int highBufferThreshold = OrganismInteractionCalculator.reproductionThreshold(fungus, highBufferEnv, 0);

        assertThat(highBufferThreshold).isEqualTo(lowBufferThreshold - 2);
        assertThat(highBufferThreshold).isEqualTo(10);
    }
}
