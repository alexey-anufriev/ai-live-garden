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
}
