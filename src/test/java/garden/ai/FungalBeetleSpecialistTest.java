package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FungalBeetleSpecialistTest {

    @Test
    public void testFungalBeetleSpecialistEfficiencyScalesWithBeetleDensity() {
        Environment env = new Environment(100, 100, 100, 100, 100);

        List<Organism> baseOrganisms = new ArrayList<>();
        baseOrganisms.add(Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-beetle-specialist"));
        
        // Scenario 1: Low Beetle Count
        List<Organism> lowBeetleOrganisms = new ArrayList<>(baseOrganisms);
        for (int i = 0; i < 100; i++) {
            lowBeetleOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 1, 1));
        }
        int contributionLow = TraitRegistry.calculateFungalContribution(lowBeetleOrganisms, env);
        
        // Scenario 2: High Beetle Count
        List<Organism> highBeetleOrganisms = new ArrayList<>(baseOrganisms);
        for (int i = 0; i < 5000; i++) {
            highBeetleOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 1, 1));
        }
        int contributionHigh = TraitRegistry.calculateFungalContribution(highBeetleOrganisms, env);
        
        assertTrue(contributionHigh > contributionLow, "Fungal beetle specialist contribution should be significantly higher with high beetle density: " + contributionHigh + " vs " + contributionLow);
    }
}
