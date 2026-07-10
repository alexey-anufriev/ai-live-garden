package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FungalMetabolicAmplifierTest {

    @Test
    public void testFungalMetabolicAmplifierContributionBonus() {
        List<Organism> organisms = new ArrayList<>();
        // Fungus with 'fungal-metabolic-amplifier'
        organisms.add(Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-metabolic-amplifier"));
        
        Environment env = new Environment(100, 100, 100, 100, 100);
        
        // Calculate fungal contribution
        int contribution = TraitRegistry.calculateFungalContribution(organisms, env);
        
        // Base contribution for 1 fungus (bufferBonus=2)
        // FungusCount(1) * 2 * bufferBonus(2) = 4
        // MetabolicAmplifierCount(1) * 150 = 150
        // Total = 154
        
        List<Organism> organismsNoTrait = new ArrayList<>();
        organismsNoTrait.add(Organism.of("fungus-2", OrganismType.FUNGUS, 10, 1));
        int contributionNoTrait = TraitRegistry.calculateFungalContribution(organismsNoTrait, env);
        // Total = 4
        
        assertTrue(contribution > contributionNoTrait, "Fungal metabolic amplifier should increase fungal contribution");
    }

    @Test
    public void testFungalMetabolicAmplifierMetabolicBonus() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "fungal-metabolic-amplifier");
        Environment env = new Environment(100, 100, 100, 100, 100);
        
        TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect("fungal-metabolic-amplifier", 1, fungus, env, 0, 0);
        
        assertEquals(15, effect.energyBonus(), "Fungal metabolic amplifier should provide an energy bonus of 15");
    }
}
