package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FungalDecompositionTraitTest {

    @Test
    public void testFungalDecompositionEfficiencyBonus() {
        List<Organism> organisms = new ArrayList<>();
        // Fungus with 'fungal-decomposition-efficiency'
        organisms.add(Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-decomposition-efficiency"));
        
        Environment env = new Environment(100, 100, 100, 100, 100);
        
        // Calculate fungal contribution
        int contribution = TraitRegistry.calculateFungalContribution(organisms, env);
        
        // Base contribution for 1 fungus (bufferBonus=2)
        // FungusCount(1) * 2 * bufferBonus(2) = 4
        // DecomposerEfficiencyCount(1) * 60 = 60
        // Total = 64
        
        List<Organism> organismsNoTrait = new ArrayList<>();
        organismsNoTrait.add(Organism.of("fungus-2", OrganismType.FUNGUS, 10, 1));
        int contributionNoTrait = TraitRegistry.calculateFungalContribution(organismsNoTrait, env);
        // Total = 4
        
        assertTrue(contribution > contributionNoTrait, "Fungal decomposition efficiency should increase fungal contribution");
    }
}
