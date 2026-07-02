package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;

class StressCalculatorTest {

    @Test
    void plantStressedWithoutFavoringEnvironment() {
        Environment env = new Environment(10, 10, 10, 50, 100); // Does not favor plants
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        assertThat(StressCalculator.isPlantStressed(moss, env, List.of())).isTrue();
    }

    @Test
    void plantNotStressedWithFavoringEnvironment() {
        Environment env = new Environment(60, 60, 60, 50, 100); // Favors plants
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        assertThat(StressCalculator.isPlantStressed(moss, env, List.of())).isFalse();
    }

    @Test
    void plantStressedInScarcityLosesEnergy() {
        Environment env = new Environment(10, 10, 10, 0, 100); // Nutrient 0
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        StressCalculator.StressResult result = StressCalculator.calculatePlantStressResult(moss, env, 1, List.of());
        assertThat(result.isStressed()).isTrue();
        assertThat(result.energyLoss()).isEqualTo(1);
        assertThat(result.event()).isPresent();
    }
    
    @Test
    void animalNotStarvingInLowNutrientsWithHighBuffer() {
        Environment env = new Environment(50, 50, 50, 20, 100); // Nutrients 20, Buffer 100 -> combined > 25
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1);
        
        assertThat(StressCalculator.isAnimalStarving(fox, env, List.of())).isFalse();
    }

    @Test
    void animalStarvingInLowNutrientsAndLowBuffer() {
        Environment env = new Environment(50, 50, 50, 10, 10); // Nutrients 10, Buffer 10 -> combined = 15 < 25
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1);
        
        assertThat(StressCalculator.isAnimalStarving(fox, env, List.of())).isTrue();
    }

    @Test
    void plantStressedDueToOvercrowding() {
        Environment env = new Environment(60, 60, 60, 50, 100); // Favors plants
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        List<Organism> manyMoss = IntStream.range(0, 5001)
                .mapToObj(i -> Organism.of("moss-" + i, OrganismType.MOSS, 10, 1))
                .collect(Collectors.toList());
        
        assertThat(StressCalculator.isPlantStressed(moss, env, manyMoss)).isTrue();
    }
}
