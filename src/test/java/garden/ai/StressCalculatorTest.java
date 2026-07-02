package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class StressCalculatorTest {

    @Test
    void plantStressedWithoutFavoringEnvironment() {
        Environment env = new Environment(10, 10, 10, 50, 100); // Does not favor plants
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        assertThat(StressCalculator.isPlantStressed(moss, env)).isTrue();
    }

    @Test
    void plantNotStressedWithFavoringEnvironment() {
        Environment env = new Environment(60, 60, 60, 50, 100); // Favors plants
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        assertThat(StressCalculator.isPlantStressed(moss, env)).isFalse();
    }

    @Test
    void plantStressedInScarcityLosesEnergy() {
        Environment env = new Environment(10, 10, 10, 0, 100); // Nutrient 0
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        StressCalculator.StressResult result = StressCalculator.calculatePlantStressResult(moss, env, 1);
        assertThat(result.isStressed()).isTrue();
        assertThat(result.energyLoss()).isEqualTo(1);
        assertThat(result.event()).isPresent();
    }
    
    @Test
    void animalStarvingInLowNutrients() {
        Environment env = new Environment(50, 50, 50, 20, 100); // Nutrients < 25
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1);
        
        assertThat(StressCalculator.isAnimalStarving(fox, env)).isTrue();
    }
}
