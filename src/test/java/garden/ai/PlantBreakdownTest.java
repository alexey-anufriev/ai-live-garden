package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class PlantBreakdownTest {
    @Test
    void nextCycleIncludesPlantBreakdownInEvents() {
        Garden garden = Garden.seed();
        Garden next = garden.nextCycle();

        assertThat(next.events()).anySatisfy(event -> 
            assertThat(event.description()).contains("Plant breakdown:"));
        assertThat(next.events()).anySatisfy(event -> 
            assertThat(event.description()).contains("Nutrient change breakdown:"));
        assertThat(next.events()).anySatisfy(event -> {
            assertThat(event.description()).contains("moss=");
            assertThat(event.description()).contains("fern=");
        });
    }

    @Test
    void testDiagnosticBreakdown() {
        // Create a hungry environment
        Environment env = new Environment(50, 50, 50, 10, 50);
        // mossCount = 100, fernCount = 50, mossReduction = 0, fernReduction = 0, mobilizerCount = 0
        // mossConsumption = 100 / 5 - 0 = 20
        // fernConsumption = 50 / 5 - 0 = 10
        // total consumption = 30
        String diagnostic = env.diagnostic(100, 50, 0, 0, 0, 0, 0, 0, 0, 0);

        assertThat(diagnostic).contains("consumption=30 [moss=20, fern=10]");
    }

    @Test
    void testDiagnosticBreakdown_UnmetDemand() {
        // Create a hungry environment
        Environment env = new Environment(50, 50, 50, 2, 100);
        // buffer release = 100 / 2 = 50
        // mossCount = 2000, fernCount = 500, consumption = 400 + 100 = 500
        // available = 2 (nutrients) + 50 (release) = 52
        // unmet = 500 - 52 = 448
        String diagnostic = env.diagnostic(2000, 500, 0, 0, 0, 0, 0, 0, 0, 0);

        assertThat(diagnostic).contains("consumption=500 [moss=400, fern=100]");
        assertThat(diagnostic).contains("unmet=448");
    }
}
