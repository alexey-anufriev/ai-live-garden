package garden.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GardenTest {

    @Test
    void seedGardenStartsWithPlantsAndAnimals() {
        Garden garden = Garden.seed();

        assertThat(garden.cycle()).isZero();
        assertThat(garden.organisms()).hasSize(7);
        assertThat(garden.plantCount()).isGreaterThan(0);
        assertThat(garden.animalCount()).isGreaterThan(0);
        assertThat(garden.events()).isNotEmpty();
    }

    @Test
    void nextCycleAdvancesPersistentWorldAndKeepsItAlive() {
        Garden garden = Garden.seed().nextCycle();

        assertThat(garden.cycle()).isEqualTo(1);
        assertThat(garden.organisms()).isNotEmpty();
        assertThat(garden.events()).anySatisfy(event -> assertThat(event.cycle()).isEqualTo(1));
    }

    @Test
    void simulationCanContinueFromExistingGarden() {
        Garden garden = Simulation.advance(Garden.seed(), 3);
        Garden continued = Simulation.advance(garden, 2);

        assertThat(continued.cycle()).isEqualTo(5);
        assertThat(continued.organisms()).isNotEmpty();
    }

    @Test
    void stateStoreRoundTripsGarden(@TempDir Path tempDir) {
        Path stateFile = tempDir.resolve("garden-state.txt");
        Garden original = Simulation.advance(Garden.seed(), 2);

        GardenStateStore.save(stateFile, original);
        Garden loaded = GardenStateStore.loadOrCreate(stateFile);

        assertThat(loaded.cycle()).isEqualTo(original.cycle());
        assertThat(loaded.nextId()).isEqualTo(original.nextId());
        assertThat(loaded.environment()).isEqualTo(original.environment());
        assertThat(loaded.organisms()).hasSameSizeAs(original.organisms());
    }

    @Test
    void rendererContainsCoreGardenInformation() {
        Garden garden = Simulation.run(2);
        String rendered = GardenRenderer.render(garden);

        assertThat(rendered).contains("AI Live Garden");
        assertThat(rendered).contains("Cycle: 2");
        assertThat(rendered).contains("Environment:");
        assertThat(rendered).contains("Balance:");
        assertThat(rendered).contains("Organisms:");
        assertThat(rendered).contains("Recent events:");
    }

    @Test
    void stressedOrStarvingOrganismsDoNotReproduce() {
        Organism stressedPlant = Organism.of("plant-1", OrganismType.FERN, 20, 1, "stressed");
        // Environment that does not favor plants (light < 40)
        Garden garden = new Garden(0, 2, new Environment(30, 50, 50, 50, 50), List.of(stressedPlant), List.of());

        Garden next = garden.nextCycle();

        assertThat(next.organisms()).hasSize(1);
        assertThat(next.organisms().get(0).id()).isEqualTo("plant-1");
    }

    @Test
    void herbivoreWithNutrientFinderTraitFeedsMoreEfficiently() {
        // Herbivore with nutrient-finder trait.
        Organism herbivore = Organism.of("herbivore-1", OrganismType.HARE, 10, 1, "nutrient-finder");
        Organism plant = Organism.of("plant-1", OrganismType.MOSS, 10, 1, "food");
        // Environment favorable.
        Environment env = new Environment(50, 50, 50, 50, 50);
        Garden garden = new Garden(0, 3, env, List.of(herbivore, plant), List.of());

        Garden next = garden.nextCycle();

        // Feeding: HARE normally gets 2 energy. With nutrient-finder, should be 2+1=3.
        // Metabolism for HARE is 1.
        // Energy: 10 - 1 (metabolism) + 3 (feeding) = 12.
        assertThat(next.organisms().stream()
                .filter(o -> o.id().equals("herbivore-1"))
                .findFirst().get().energy()).isEqualTo(12);
    }

    @Test
    void resilientOrganismsDoNotGetStressedOrStarving() {
        Organism resilientPlant = Organism.of("plant-1", OrganismType.FERN, 20, 1, "resilient");
        // Environment that does not favor plants (light < 40)
        Garden gardenPlant = new Garden(0, 2, new Environment(30, 50, 50, 50, 50), List.of(resilientPlant), List.of());
        Garden nextPlant = gardenPlant.nextCycle();
        assertThat(nextPlant.organisms().get(0).traits()).doesNotContain("stressed");

        Organism resilientAnimal = Organism.of("animal-1", OrganismType.HARE, 20, 1, "resilient");
        // Environment with low nutrients (< 25)
        Garden gardenAnimal = new Garden(0, 2, new Environment(50, 50, 50, 20, 50), List.of(resilientAnimal), List.of());
        Garden nextAnimal = gardenAnimal.nextCycle();
        assertThat(nextAnimal.organisms().get(0).traits()).doesNotContain("starving");
    }

    @Test
    void resilientAnimalsHaveHigherMetabolism() {
        // HARE has metabolism 1. With resilient, should be 2.
        Organism resilientAnimal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "resilient");
        // Environment favorable to prevent starving
        Garden garden = new Garden(0, 2, new Environment(50, 50, 50, 50, 50), List.of(resilientAnimal), List.of());
        Garden next = garden.nextCycle();
        // 10 - 2 = 8
        assertThat(next.organisms().get(0).energy()).isEqualTo(8);
    }

    @Test
    void resilientPlantsHaveLowerGrowth() {
        // FERN has growth 2 (in favorable env). With resilient, should be 1.
        Organism resilientPlant = Organism.of("plant-1", OrganismType.FERN, 10, 1, "resilient");
        // Environment favorable for plants
        Garden garden = new Garden(0, 2, new Environment(50, 50, 50, 50, 50), List.of(resilientPlant), List.of());
        Garden next = garden.nextCycle();
        // 10 + 1 = 11
        assertThat(next.organisms().get(0).energy()).isEqualTo(11);
    }


    @Test
    void mossGrowsFasterInHighMoisture() {
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1, "test");
        // Environment that favors plants (default 50) and high moisture (70)
        Environment env = new Environment(50, 70, 50, 50, 50);
        Garden garden = new Garden(0, 2, env, List.of(moss), List.of());

        Garden next = garden.nextCycle();

        // Passive change growth: 2 (favorsPlants) + 1 (moisture > 60) = 3
        assertThat(next.organisms().get(0).energy()).isEqualTo(13);
    }

    @Test
    void sunLoverPlantsGrowFasterInSun() {
        // Plant with sun-lover trait.
        Organism plant = Organism.of("plant-1", OrganismType.FERN, 10, 1, "sun-lover");
        // Environment light=70 (> 60), favorsPlants is true (light >= 45, moisture >= 45, nutrients >= 30)
        Environment env = new Environment(70, 50, 50, 50, 50);
        Garden garden = new Garden(0, 2, env, List.of(plant), List.of());

        Garden next = garden.nextCycle();

        // Passive change growth: 2 (favorsPlants) + 1 (sun-lover + high light) = 3
        assertThat(next.organisms().get(0).energy()).isEqualTo(13);
    }

    @Test
    void rainCollectorPlantsGrowFasterInDryConditions() {
        // Plant with rain-collector trait.
        Organism plant = Organism.of("plant-1", OrganismType.FERN, 10, 1, "rain-collector");
        // Environment moisture=30 (< 40), favorsPlants is false (default env).
        // To ensure favorsPlants is false, I'll set light=30 (< 45).
        Environment env = new Environment(30, 30, 50, 50, 50);
        Garden garden = new Garden(0, 2, env, List.of(plant), List.of());

        Garden next = garden.nextCycle();

        // Passive change growth: 0 (favorsPlants=false) + 1 (rain-collector + moisture < 40) = 1
        assertThat(next.organisms().get(0).energy()).isEqualTo(11);
    }

    @Test
    void deathsIncreaseNutrients() {
        // An organism that will die (no energy)
        Organism doomed = Organism.of("beetle-1", OrganismType.BEETLE, 1, 1, "doomed");
        // Environment with 50 nutrients
        Environment env = new Environment(50, 50, 50, 50, 50);
        Garden garden = new Garden(0, 2, env, List.of(doomed), List.of());

        Garden next = garden.nextCycle();

        assertThat(next.environment().nutrients()).isEqualTo(59);
    }

    @Test
    void rootNetworkIncreasesNutrients() {
        // One root network
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "network");
        // Environment with 50 nutrients
        Environment env = new Environment(50, 50, 50, 50, 50);
        Garden garden = new Garden(0, 2, env, List.of(root), List.of());

        Garden next = garden.nextCycle();

        assertThat(next.environment().nutrients()).isEqualTo(57);
    }

    @Test
    void rootNetworkIncreasesNutrientsSignificantlyWhenHungry() {
        // One root network
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "network");
        // Environment with 20 nutrients (hungry)
        Environment env = new Environment(50, 50, 50, 20, 50);
        Garden garden = new Garden(0, 2, env, List.of(root), List.of());

        Garden next = garden.nextCycle();

        assertThat(next.environment().nutrients()).isEqualTo(27);
    }

    @Test
    void rootNetworkIncreasesNutrientsEvenMoreWhenVeryHungry() {
        // One root network
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "network");
        // Environment with 5 nutrients (very hungry)
        Environment env = new Environment(50, 50, 50, 5, 50);
        Garden garden = new Garden(0, 2, env, List.of(root), List.of());

        Garden next = garden.nextCycle();

        assertThat(next.environment().nutrients()).isEqualTo(12);
    }

    @Test
    void nutrientEfficientPlantsGrowFasterInHungryConditions() {
        // Plant with nutrient-efficient trait.
        Organism plant = Organism.of("plant-1", OrganismType.FERN, 10, 1, "nutrient-efficient");
        // Environment light=50 (favorsPlants), nutrients=20 (< 30, hungry)
        Environment env = new Environment(50, 50, 50, 20, 50);
        Garden garden = new Garden(0, 2, env, List.of(plant), List.of());

        Garden next = garden.nextCycle();

        // Passive change growth: 0 (favorsPlants=false) + 1 (nutrient-efficient + nutrients < 30) = 1
        assertThat(next.organisms().get(0).energy()).isEqualTo(11);
    }

    @Test
    void organismAtCriticalEnergyTriggersEvent() {
        // An organism with 3 energy. Metabolism for HARE is 1.
        // After one cycle, energy should be 2.
        Organism animal = Organism.of("animal-1", OrganismType.HARE, 3, 1, "test");
        // Environment favorable to prevent starving
        Garden garden = new Garden(0, 2, new Environment(50, 50, 50, 50, 50), List.of(animal), List.of());
        Garden next = garden.nextCycle();

        assertThat(next.organisms().get(0).energy()).isEqualTo(2);
        assertThat(next.events()).anyMatch(e -> e.description().contains("animal-1 is at a critical energy level."));
    }

    @Test
    void preyWithShadowStepperTraitCanAvoidPredators() {
        Organism predator = Organism.of("fox-1", OrganismType.FOX, 10, 1, "hunter");
        // Prey with shadow-stepper.
        Organism prey1 = Organism.of("hare-0", OrganismType.HARE, 10, 1, "shadow-stepper");
        // Prey without shadow-stepper.
        Organism prey2 = Organism.of("hare-1", OrganismType.HARE, 10, 1, "standard");
        // Environment favorable.
        Environment env = new Environment(50, 50, 50, 50, 50);
        // Order: hare-0, hare-1, fox-1.
        Garden garden = new Garden(0, 3, env, List.of(predator, prey1, prey2), List.of());

        Garden next = garden.nextCycle();

        boolean fedOnStealthy = next.events().stream().anyMatch(e -> e.description().contains("fox-1 fed on hare-0"));
        boolean fedOnStandard = next.events().stream().anyMatch(e -> e.description().contains("fox-1 fed on hare-1"));

        // If predator fed on stealthy, it shouldn't have fed on standard.
        // If predator didn't feed on stealthy, it must have fed on standard.
        if (fedOnStealthy) {
            assertThat(fedOnStandard).isFalse();
        } else {
            assertThat(fedOnStandard).isTrue();
        }
    }

    @Test
    void hardyFernsGrowFasterInWarmConditions() {
        // Fern with hardy trait.
        Organism fern = Organism.of("fern-1", OrganismType.FERN, 10, 1, "hardy");
        // Environment favorable for plant growth (default 50) and warmth=60 (> 50).
        Environment env = new Environment(50, 50, 60, 50, 50);
        Garden garden = new Garden(0, 2, env, List.of(fern), List.of());

        Garden next = garden.nextCycle();

        // Passive change growth: 2 (favorsPlants) + 1 (hardy + warmth > 50) = 3
        assertThat(next.organisms().get(0).energy()).isEqualTo(13);
    }

    @Test
    void waterSeekerMossGrowsFasterInDryConditions() {
        // Moss with water-seeker trait.
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1, "water-seeker");
        // Environment light=50 (favorsPlants=true), moisture=46 (>= 45, < 50).
        Environment env = new Environment(50, 46, 50, 50, 50);
        Garden garden = new Garden(0, 2, env, List.of(moss), List.of());

        Garden next = garden.nextCycle();

        // Passive change growth: 2 (favorsPlants) + 1 (water-seeker + moisture < 50) = 3
        assertThat(next.organisms().get(0).energy()).isEqualTo(13);
    }

    @Test
    void dormantOrganismsAreResilientAndEfficientInHunger() {
        // Animal with dormancy, in very low nutrients (<15).
        Organism dormantAnimal = Organism.of("animal-1", OrganismType.HARE, 10, 1, "dormancy");
        // Environment with nutrients=10 (< 15). Metabolism: 1. With dormancy, it should be 1-2 = -1, but Math.max(0, ...) makes it 0.
        Environment env = new Environment(50, 50, 50, 10, 0);
        Garden garden = new Garden(0, 2, env, List.of(dormantAnimal), List.of());
        Garden next = garden.nextCycle();
        // Energy: 10 - 0 = 10.
        assertThat(next.organisms().get(0).energy()).isEqualTo(10);
        assertThat(next.organisms().get(0).traits()).doesNotContain("starving");

        // Plant with dormancy, in low nutrients (<15).
        Organism dormantPlant = Organism.of("plant-1", OrganismType.FERN, 10, 1, "dormancy");
        // Environment light=30 (does not favor plants), nutrients=10 (< 15).
        Environment envPlant = new Environment(30, 30, 30, 10, 0);
        Garden gardenPlant = new Garden(0, 2, envPlant, List.of(dormantPlant), List.of());
        Garden nextPlant = gardenPlant.nextCycle();
        // Should not be stressed.
        assertThat(nextPlant.organisms().get(0).traits()).doesNotContain("stressed");
    }
}
