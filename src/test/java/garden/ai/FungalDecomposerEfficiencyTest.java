package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FungalDecomposerEfficiencyTest {

    @Test
    public void testMassDecomposerBonus() {
        List<Organism> organisms = new ArrayList<>();
        // Fungus with 'mass-decomposer'
        organisms.add(Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "mass-decomposer"));
        
        // Add 100 beetles to create decay pressure (decayPressure = 1)
        for (int i = 0; i < 100; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 1));
        }
        
        Environment env = new Environment(100, 100, 100, 100, 100);
        
        // Calculate fungal contribution
        int contribution = TraitRegistry.calculateFungalContribution(organisms, env);
        
        // Contribution should be significantly higher with the bonus
        // Without mass-decomposer: fungusCount * 2 * bufferBonus + ...
        // With mass-decomposer: fungusCount * 2 * bufferBonus + ... + massDecomposerCount * decayPressure * 10
        // DecayPressure = 100/100 = 1. Bonus = 1 * 1 * 10 = 10.
        // This is a test, the exact number is less important than ensuring it's higher than the base.
        
        // Base contribution for 1 fungus (bufferBonus=2, decayPressure=1)
        // Fungus count (1) * 2 * bufferBonus (2) = 4
        // DecomposerCount (0) * (30+1) * 2 = 0
        // ...
        
        // It's easier to verify it's higher than a fungus without the trait.
        
        List<Organism> organismsNoTrait = new ArrayList<>();
        organismsNoTrait.add(Organism.of("fungus-2", OrganismType.FUNGUS, 10, 1));
        for (int i = 0; i < 100; i++) {
            organismsNoTrait.add(Organism.of("beetle-base-" + i, OrganismType.BEETLE, 10, 1));
        }
        int contributionNoTrait = TraitRegistry.calculateFungalContribution(organismsNoTrait, env);
        
        assertTrue(contribution > contributionNoTrait, "Mass decomposer should increase fungal contribution under pressure");
    }
}
