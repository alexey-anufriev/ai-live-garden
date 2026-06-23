package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class BufferReleaseEventTest {

    @Test
    void bufferReleaseGeneratesEvent() {
        // Environment with 0 nutrients (releaseRate=2) and 100 buffer
        Environment env = new Environment(50, 50, 50, 0, 100);
        // Organisms that don't contribute to buffer/nutrients, to isolate buffer release
        Organism plant = Organism.of("plant-1", OrganismType.FERN, 10, 1, "shade-loving");
        Garden garden = new Garden(0, 2, env, List.of(plant), List.of());

        Garden next = garden.nextCycle();

        assertThat(next.events()).anySatisfy(event -> 
            assertThat(event.description()).contains("Nutrients released from the buffer."));
    }
}
