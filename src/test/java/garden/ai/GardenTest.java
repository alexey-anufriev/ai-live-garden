package garden.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GardenTest {

    @Test
    void seedGardenStartsWithThreeOrganisms() {
        Garden garden = Garden.seed();

        assertThat(garden.cycle()).isZero();
        assertThat(garden.organisms().size()).isEqualTo(3);
        assertThat(garden.events()).isNotEmpty();
    }

    @Test
    void nextCycleAdvancesGardenAndKeepsOrganisms() {
        Garden garden = Garden.seed().nextCycle();

        assertThat(garden.cycle()).isEqualTo(1);
        assertThat(garden.organisms().size()).isEqualTo(3);
        assertThat(garden.events()).anySatisfy(event -> assertThat(event.cycle()).isEqualTo(1));
    }

    @Test
    void rendererContainsCoreGardenInformation() {
        Garden garden = Simulation.run(2);
        String rendered = GardenRenderer.render(garden);

        assertThat(rendered).contains("AI Live Garden");
        assertThat(rendered).contains("Cycle: 2");
        assertThat(rendered).contains("Organisms:");
        assertThat(rendered).contains("Recent events:");
    }
}
