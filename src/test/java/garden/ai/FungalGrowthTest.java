package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class FungalGrowthTest {
    @Test
    void fungalGrowsWithAvailableNutrients() {
        Environment nutrientRich = new Environment(50, 50, 50, 50, 100);
        List<Organism> organisms = new ArrayList<>();
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        OrganismInteractionCalculator.PassiveChangeContext context = new OrganismInteractionCalculator.PassiveChangeContext(
            nutrientRich, 0, new ArrayList<>(), new TraitRegistry.ContributionResult(0, 0, 0, 0), List.of(fungus)
        );
        
        List<Organism> changed = OrganismInteractionCalculator.calculatePassiveChanges(context);
        assertThat(changed.get(0).energy()).isGreaterThan(fungus.energy());
    }

    @Test
    void fungalGrowsAtModerateNutrientLevels() {
        Environment moderateNutrients = new Environment(50, 50, 50, 15, 100);
        List<Organism> organisms = new ArrayList<>();
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1);
        OrganismInteractionCalculator.PassiveChangeContext context = new OrganismInteractionCalculator.PassiveChangeContext(
            moderateNutrients, 0, new ArrayList<>(), new TraitRegistry.ContributionResult(0, 0, 0, 0), List.of(fungus)
        );
        
        List<Organism> changed = OrganismInteractionCalculator.calculatePassiveChanges(context);
        assertThat(changed.get(0).energy()).isGreaterThan(fungus.energy());
    }
}
