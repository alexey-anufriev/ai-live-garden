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
        
        assertThat(TraitRegistry.isPlantStressed(moss, env, List.of())).isTrue();
    }

    @Test
    void plantNotStressedWithFavoringEnvironment() {
        Environment env = new Environment(60, 60, 60, 50, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        assertThat(TraitRegistry.isPlantStressed(moss, env, List.of())).isFalse();
    }

    @Test
    void plantStressedInScarcityLosesEnergy() {
        Environment env = new Environment(10, 10, 10, 0, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        
        TraitRegistry.StressResult result = TraitRegistry.calculatePlantStress(moss, env, 1, List.of());
        assertThat(result.isStressed()).isTrue();
        assertThat(result.energyLoss()).isEqualTo(1);
        assertThat(result.event()).isNotNull();
    }
    
    @Test
    void animalNotStarvingInLowNutrientsWithHighBuffer() {
        Environment env = new Environment(50, 50, 50, 20, 100);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1);
        
        assertThat(TraitRegistry.isAnimalStarving(fox, env, List.of())).isFalse();
    }

    @Test
    void animalStarvingInLowNutrientsAndLowBuffer() {
        Environment env = new Environment(50, 50, 50, 10, 10);
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 10, 1);
        
        assertThat(TraitRegistry.isAnimalStarving(fox, env, List.of())).isTrue();
    }

    @Test
    void plantStressedDueToOvercrowding() {
        Environment env = new Environment(60, 60, 60, 50, 100);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1);
        List<Organism> manyMoss = IntStream.range(0, 5001)
                .mapToObj(i -> Organism.of("moss-" + i, OrganismType.MOSS, 10, 1))
                .collect(Collectors.toList());
        
        assertThat(TraitRegistry.isPlantStressed(moss, env, manyMoss)).isTrue();
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

    @Test
    void highBeetlePopulationIncreasesNutrientConsumption() {
        Environment env = new Environment(50, 50, 50, 100, 50);
        
        // Scenario 1: Low beetle count (1000), plantCount (50000)
        List<Organism> lowBeetleOrganisms = IntStream.range(0, 1000)
                .mapToObj(i -> Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 1))
                .collect(Collectors.toList());
        List<Organism> plants1 = IntStream.range(0, 50000)
                .mapToObj(i -> Organism.of("moss-" + i, OrganismType.MOSS, 10, 1))
                .collect(Collectors.toList());
        lowBeetleOrganisms.addAll(plants1);

        OrganismInteractionCalculator.EnvironmentalDynamicsContext lowContext = new OrganismInteractionCalculator.EnvironmentalDynamicsContext(
            lowBeetleOrganisms, env, 1, new ArrayList<>());
        
        OrganismInteractionCalculator.EnvironmentalDynamicsResult lowResult = 
            OrganismInteractionCalculator.calculateEnvironmentalDynamics(lowContext);
        
        int lowConsumption = lowResult.nextEnvironment().nutrients() - (env.nutrients() + 2 + lowBeetleOrganisms.size() / 2);

        // Scenario 2: High beetle count (3000), plantCount (50000)
        List<Organism> highBeetleOrganisms = IntStream.range(0, 3000)
                .mapToObj(i -> Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 1))
                .collect(Collectors.toList());
        List<Organism> plants2 = IntStream.range(0, 50000)
                .mapToObj(i -> Organism.of("moss-" + i, OrganismType.MOSS, 10, 1))
                .collect(Collectors.toList());
        highBeetleOrganisms.addAll(plants2);
        
        OrganismInteractionCalculator.EnvironmentalDynamicsContext highContext = new OrganismInteractionCalculator.EnvironmentalDynamicsContext(
            highBeetleOrganisms, env, 1, new ArrayList<>());

        OrganismInteractionCalculator.EnvironmentalDynamicsResult highResult = 
            OrganismInteractionCalculator.calculateEnvironmentalDynamics(highContext);

        int highConsumption = highResult.nextEnvironment().nutrients() - (env.nutrients() + 2 + highBeetleOrganisms.size() / 2);

        // Verification: High beetle population should result in higher consumption (more negative nutrient change)
        assertThat(highConsumption).isLessThan(lowConsumption);
    }


    @Test
    void cautiousFeederFoxSkipsFeedingWithLowBeetleDensity() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 20, 1, "cautious-feeder");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1);
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        // Low beetle population (1 beetle)
        List<Organism> organisms = new ArrayList<>(List.of(fox, beetle));
        
        OrganismInteractionCalculator.FeedingResult result = OrganismInteractionCalculator.calculateFeeding(
                new OrganismInteractionCalculator.FeedingPhaseContext(organisms, env, 1, new ArrayList<>()));

        Organism fedFox = result.organisms().stream()
                .filter(o -> o.id().equals("fox-1"))
                .findFirst().get();
        
        // Should have skipped feeding, energy remains 20
        assertThat(fedFox.energy()).isEqualTo(20);
    }

    @Test
    void cautiousFeederFoxFeedsWithHighBeetleDensity() {
        Organism fox = Organism.of("fox-1", OrganismType.FOX, 20, 1, "cautious-feeder");
        Organism beetle = Organism.of("beetle-1", OrganismType.BEETLE, 10, 1);
        List<Organism> manyBeetles = IntStream.range(0, 4001)
                .mapToObj(i -> Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 1))
                .collect(Collectors.toList());
        List<Organism> organisms = new ArrayList<>(List.of(fox, beetle));
        organisms.addAll(manyBeetles);
        
        Environment env = new Environment(50, 50, 50, 50, 50);
        
        OrganismInteractionCalculator.FeedingResult result = OrganismInteractionCalculator.calculateFeeding(
                new OrganismInteractionCalculator.FeedingPhaseContext(organisms, env, 1, new ArrayList<>()));

        Organism fedFox = result.organisms().stream()
                .filter(o -> o.id().equals("fox-1"))
                .findFirst().get();
        
        // Should have fed, energy increases
        assertThat(fedFox.energy()).isGreaterThan(20);
    }

    @Test
    void foxCullingAggressiveAtHighPopulation() {
        Environment env = new Environment(50, 50, 50, 100, 100);

        // Create 1500 foxes
        List<Organism> organisms = IntStream.range(0, 1500)
                .mapToObj(i -> Organism.of("fox-" + i, OrganismType.FOX, 20, 1))
                .collect(Collectors.toList());

        OrganismInteractionCalculator.PassiveChangeContext context = new OrganismInteractionCalculator.PassiveChangeContext(
                env, 1, new ArrayList<>(), new TraitRegistry.ContributionResult(0,0,0,0), organisms);

        List<Organism> changed = OrganismInteractionCalculator.calculatePassiveChanges(context);

        // Many foxes should have reduced energy (energy < 20)
        long culledFoxes = changed.stream()
                .filter(o -> o.type() == OrganismType.FOX && o.energy() < 20)
                .count();

        assertThat(culledFoxes).isEqualTo(1500);
    }

    @Test
    void beetleReproductionRestrictedAtHighPopulation() {
        Environment env = new Environment(50, 50, 50, 100, 100);
        // Create 14000 total organisms, 3001 of them being beetles
        List<Organism> organisms = IntStream.range(0, 14000)
                .mapToObj(i -> {
                    if (i < 3001) {
                        return Organism.of("beetle-" + i, OrganismType.BEETLE, 10, 1);
                    } else {
                        return Organism.of("moss-" + i, OrganismType.MOSS, 10, 1);
                    }
                })
                .collect(Collectors.toList());

        // Should be restricted
        int budget = OrganismInteractionCalculator.typeBirthBudget(OrganismType.BEETLE, organisms, env);
        assertThat(budget).isEqualTo(0);
    }

    @Test
    void highBufferIncreasesFungalNutrientContribution() {
        Environment lowBufferEnv = new Environment(50, 50, 50, 50, 10);
        Environment highBufferEnv = new Environment(50, 50, 50, 50, 100);

        // Setup: just one fungus
        List<Organism> organisms = List.of(Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1));

        OrganismInteractionCalculator.EnvironmentalDynamicsContext lowContext = new OrganismInteractionCalculator.EnvironmentalDynamicsContext(
                organisms, lowBufferEnv, 1, new ArrayList<>());
        OrganismInteractionCalculator.EnvironmentalDynamicsContext highContext = new OrganismInteractionCalculator.EnvironmentalDynamicsContext(
                organisms, highBufferEnv, 1, new ArrayList<>());

        OrganismInteractionCalculator.EnvironmentalDynamicsResult lowResult = OrganismInteractionCalculator.calculateEnvironmentalDynamics(lowContext);
        OrganismInteractionCalculator.EnvironmentalDynamicsResult highResult = OrganismInteractionCalculator.calculateEnvironmentalDynamics(highContext);

        // Fungal contribution is increased by the buffer, leading to more nutrients
        assertThat(highResult.nextEnvironment().nutrients()).isGreaterThan(lowResult.nextEnvironment().nutrients());
    }
}

