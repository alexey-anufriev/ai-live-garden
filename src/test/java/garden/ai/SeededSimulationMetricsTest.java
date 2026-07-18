package garden.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SeededSimulationMetricsTest {

    @Test
    void sameSeedProducesSameReadOnlyReport() {
        Garden garden = Garden.seed();

        SimulationMetrics.Report first = SimulationMetrics.evaluate(garden, 8, 42L, 20_000);
        SimulationMetrics.Report second = SimulationMetrics.evaluate(garden, 8, 42L, 20_000);

        assertThat(second.toJson()).isEqualTo(first.toJson());
        assertThat(first.completedSteps()).isEqualTo(8);
        assertThat(first.status()).isEqualTo("completed");
    }

    @Test
    void simulateCommandDoesNotModifyState(@TempDir Path tempDir) throws Exception {
        Path state = tempDir.resolve("garden-state.txt");
        GardenStateStore.save(state, Garden.seed());
        String before = Files.readString(state);

        Main.main(new String[]{"simulate", "--state", state.toString(), "--steps", "3", "--seed", "7"});

        assertThat(Files.readString(state)).isEqualTo(before);
    }

    @Test
    void populationLimitStopsRunBeforeRequestedHorizon() {
        SimulationMetrics.Report report = SimulationMetrics.evaluate(Garden.seed(), 20, 1L, 1);

        assertThat(report.status()).isEqualTo("population-limit-exceeded");
        assertThat(report.completedSteps()).isLessThan(20);
    }
}
