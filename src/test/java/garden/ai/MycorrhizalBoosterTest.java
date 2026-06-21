package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

public class MycorrhizalBoosterTest {

    @Test
    public void testMycorrhizalBoosterBoostsFungalFeeder() {
        Organism fungus1 = Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1, "mycorrhizal-booster");
        // Start with 5 energy, below reproduction threshold (14)
        Organism moss1 = Organism.of("moss-1", OrganismType.MOSS, 5, 1, "fungal-feeder");
        
        // Fungal contribution will be > 0 because of fungus1.
        Garden garden = new Garden(1, 1, new Environment(50, 50, 50, 50, 50), List.of(fungus1, moss1), List.of());
        
        Garden nextGarden = garden.nextCycle();
        
        Organism boostedMoss = nextGarden.organisms().stream().filter(o -> o.id().equals("moss-1")).findFirst().get();
        
        // Base energy 5, +2 from favorsPlants, +2 from boosting (fungal-feeder + booster) = 9
        assertTrue(boostedMoss.energy() >= 9, "Moss energy should be boosted, was: " + boostedMoss.energy());
        assertTrue(nextGarden.events().stream().anyMatch(e -> e.description().contains("(boosted)")), "Event should mention boosting.");
    }
}
