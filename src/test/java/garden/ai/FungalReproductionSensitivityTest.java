package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class FungalReproductionSensitivityTest {
    @Test
    void fungalThresholdDecreasesWithLowNutrients() {
        Environment lowNutrients = new Environment(50, 50, 50, 10, 100);
        Environment normalNutrients = new Environment(50, 50, 50, 50, 100);
        List<Organism> organisms = new ArrayList<>();
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        
        // threshold = 8.
        // buffer = 100 (>50), threshold -= 4.
        // fungusCount = 1 (<5000), threshold -= 3.
        // low nutrients (10 < 25), threshold -= 1.
        // lowNutrients: 8 - 4 - 3 - 1 = 0.
        // normalNutrients: 8 - 4 - 3 = 1.
        
        int thresholdLow = OrganismInteractionCalculator.reproductionThreshold(fungus, lowNutrients, 0, organisms);
        int thresholdNormal = OrganismInteractionCalculator.reproductionThreshold(fungus, normalNutrients, 0, organisms);
        
        assertThat(thresholdLow).isEqualTo(0);
        assertThat(thresholdNormal).isEqualTo(1);
    }
}
