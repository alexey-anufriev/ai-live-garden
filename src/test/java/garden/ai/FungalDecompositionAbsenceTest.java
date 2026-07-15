package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FungalDecompositionAbsenceTest {

    @Test
    public void testFungalDecompositionEfficiencyIncreasesWhenBeetlesAreAbsent() {
        Environment env = new Environment(100, 100, 100, 100, 100);

        List<Organism> baseOrganisms = new ArrayList<>();
        baseOrganisms.add(Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-decomposition-efficiency"));
        
        // Scenario 1: No Beetles (Absence)
        List<Organism> noBeetleOrganisms = new ArrayList<>(baseOrganisms);
        int contributionNoBeetles = TraitRegistry.calculateFungalContribution(noBeetleOrganisms, env);
        
        // Scenario 2: Low Beetle Count
        List<Organism> lowBeetleOrganisms = new ArrayList<>(baseOrganisms);
        for (int i = 0; i < 100; i++) {
            lowBeetleOrganisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 1, 1));
        }
        int contributionLow = TraitRegistry.calculateFungalContribution(lowBeetleOrganisms, env);
        
        assertTrue(contributionNoBeetles > contributionLow, "Fungal decomposition contribution should be higher when beetles are absent than with low beetle density to compensate for lack of prey turnover: " + contributionNoBeetles + " vs " + contributionLow);
    }
}
