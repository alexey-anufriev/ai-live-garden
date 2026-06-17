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
        Garden garden = new Garden(0, 2, new Environment(30, 50, 50, 50), List.of(stressedPlant), List.of());

        Garden next = garden.nextCycle();

        assertThat(next.organisms()).hasSize(1);
        assertThat(next.organisms().get(0).id()).isEqualTo("plant-1");
    }
}
