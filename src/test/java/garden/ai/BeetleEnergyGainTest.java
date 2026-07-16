package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class BeetleEnergyGainTest {
    @Test
    public void testLoneBeetleWithRecoveryTraitGainsEnergy() {
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 5, 1, "test");
        beetle = beetle.withTrait("beetle-recovery");
        
        List<Organism> organisms = new ArrayList<>();
        organisms.add(beetle);
        
        Environment env = new Environment(100, 100, 100, 100, 100);
        
        TraitRegistry.ContributionResult contribution = new TraitRegistry.ContributionResult(0, 0, 0, 0);
        
        OrganismInteractionCalculator.PassiveChangeContext context = new OrganismInteractionCalculator.PassiveChangeContext(
            env, 1, new ArrayList<>(), contribution, organisms
        );
        
        Organism changed = OrganismInteractionCalculator.calculatePassiveChanges(context).get(0);
        
        // Base energy 5 + trait energy 15 = 20. Metabolism is 0 if not specified otherwise.
        assertEquals(19, changed.energy(), "Beetle with beetle-recovery should gain 15 energy, minus 1 base metabolism");
    }
}
