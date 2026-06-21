package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ResourcefulBreederTest {

    @Test
    void resourcefulBreederReducesReproductionThresholdWhenNutrientsAreScarce() {
        // Setup: Low nutrients, organism with resourceful-breeder
        Environment environment = new Environment(10, 10, 10, 10, 0);
        
        // Reproduction threshold for Hare is 15 (base).
        // With resourceful-breeder and nutrients < 20, threshold becomes 15 - 3 = 12.
        // Starting with 13 energy. After metabolism (1), energy will be 12. 
        // 12 >= 12, so it SHOULD reproduce.
        
        Organism organism = Organism.of("hare-1", OrganismType.HARE, 13, 1, "resourceful-breeder");

        // Need to check Garden's reproduction logic or manually trigger it.
        // The reproduction logic is private, so we test it through the garden cycle.
        
        java.util.List<Organism> organisms = java.util.List.of(organism);
        Garden gardenWithOrganism = new Garden(1, 2, environment, organisms, new java.util.ArrayList<>());

        // We can't directly call reproductionPhase, but we can call nextCycle, 
        // which calls reproductionPhase internally.
        Garden nextGarden = gardenWithOrganism.nextCycle();
        
        // The hare should have reproduced if it had enough energy.
        // nextGarden should have more than 1 organism if reproduction occurred.
        assertThat(nextGarden.organisms()).hasSizeGreaterThan(1);
    }
}
