package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class BufferStabilizerTest {
    @Test
    public void testBufferStabilizerBoost() {
        Environment env = new Environment(100, 100, 100, 0, 50);
        Organism animal = new Organism("animal-1", OrganismType.FOX, 10, 0, 0, List.of("buffer-stabilizer"));
        Organism fungus = new Organism("fungus-1", OrganismType.FUNGUS, 10, 0, 0, List.of());
        
        Garden garden = new Garden(1, 2, env, List.of(animal, fungus), List.of());
        
        // Trigger a cycle
        Garden next = garden.nextCycle();
        
        // Fungal contribution check: if buffer stabilized, buffer should increase
        assertThat(next.environment().nutrientBuffer()).isEqualTo(29);
    }
}
