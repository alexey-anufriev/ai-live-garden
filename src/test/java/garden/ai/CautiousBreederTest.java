package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class CautiousBreederTest {
    @Test
    void cautiousBreederDoesNotReproduceWhenNutrientsLow() {
        // Create one plant with the 'cautious-breeder' trait and enough energy to reproduce
        Organism plant = Organism.of("plant-1", OrganismType.MOSS, 20, 1, "cautious-breeder");
        
        // Environment with low nutrients (5)
        Environment env = new Environment(50, 50, 50, 5, 50);
        Garden garden = new Garden(1, 100, env, List.of(plant), List.of());
        
        // Advance cycle
        Garden next = garden.nextCycle();
        
        // Should only have 1 organism (the plant, reproduction prevented)
        assertThat(next.organisms()).hasSize(1);
    }
    
    @Test
    void cautiousBreederReproducesWhenNutrientsHigh() {
        // Create one plant with the 'cautious-breeder' trait and enough energy to reproduce
        Organism plant = Organism.of("plant-1", OrganismType.MOSS, 20, 1, "cautious-breeder");
        
        // Environment with high nutrients (50)
        Environment env = new Environment(50, 50, 50, 50, 50);
        Garden garden = new Garden(1, 100, env, List.of(plant), List.of());
        
        // Advance cycle
        Garden next = garden.nextCycle();
        
        // Should have reproduced
        assertThat(next.organisms()).hasSize(2);
    }
}
