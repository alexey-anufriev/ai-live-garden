package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;

class OrganismInteractionCalculatorTest {

    // --- Passive Change Tests ---

    @Test
    void plantStressedWithoutFavoringEnvironment() {
        Environment env = new Environment(10, 10, 10, 50, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        assertThat(OrganismInteractionCalculator.isPlantStressed(moss, env, List.of())).isTrue();
    }

    @Test
    void plantNotStressedWithFavoringEnvironment() {
        Environment env = new Environment(60, 60, 60, 50, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        assertThat(OrganismInteractionCalculator.isPlantStressed(moss, env, List.of())).isFalse();
    }

    @Test
    void plantStressedInScarcityLosesEnergy() {
        Environment env = new Environment(10, 10, 10, 0, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        OrganismInteractionCalculator.StressResult result = OrganismInteractionCalculator.calculatePlantStressResult(moss, env, 1, List.of());
        assertThat(result.isStressed()).isTrue();
        assertThat(result.energyLoss()).isEqualTo(1);
        assertThat(result.event()).isPresent();
    }
    
    @Test
    void animalNotStarvingInLowNutrientsWithHighBuffer() {
        Environment env = new Environment(50, 50, 50, 20, 100);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1);
        
        assertThat(OrganismInteractionCalculator.isAnimalStarving(fox, env, List.of())).isFalse();
    }

    @Test
    void animalStarvingInLowNutrientsAndLowBuffer() {
        Environment env = new Environment(50, 50, 50, 10, 10);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1);
        
        assertThat(OrganismInteractionCalculator.isAnimalStarving(fox, env, List.of())).isTrue();
    }

    @Test
    void plantStressedDueToOvercrowding() {
        Environment env = new Environment(60, 60, 60, 50, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        List<Organism> manyMoss = IntStream.range(0, 5001)
                .mapToObj(i -> Organism.of("moss-" + i, OrganismType.MOSS, 10, 1))
                .collect(Collectors.toList());
        
        assertThat(OrganismInteractionCalculator.isPlantStressed(moss, env, manyMoss)).isTrue();
    }

    // --- Feeding Phase Tests ---

    @Test
    void herbivoreWithNutrientFinderTraitFeedsMoreEfficiently() {
        Organism herbivore = Organism.of("herbivore-1", OrganismType.HARE, 10, 2, "nutrient-finder");
        Organism plant = Organism.of("plant-1", OrganismType.MOSS, 10, 1, "food");
        Environment env = new Environment(50, 50, 50, 50, 50);
        List<Organism> organisms = new ArrayList<>(List.of(herbivore, plant));
        
        OrganismInteractionCalculator.FeedingResult result = OrganismInteractionCalculator.calculateFeeding(
                new OrganismInteractionCalculator.FeedingPhaseContext(organisms, env, 1, new ArrayList<>()));

        Organism fedHerbivore = result.organisms().stream()
                .filter(o -> o.id().equals("herbivore-1"))
                .findFirst().get();
        
        assertThat(fedHerbivore.energy()).isEqualTo(13);
    }
}
