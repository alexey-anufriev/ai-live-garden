package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TestTraits {
    @Test
    void testTraits() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 100, 5, "stressed", "fungal-symbiote");
        assertTrue(root.traits().contains("stressed"));
        assertTrue(root.traits().contains("fungal-symbiote"));
    }
}
