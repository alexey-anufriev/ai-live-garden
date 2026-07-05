package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SporeProducerTest {

    @Test
    public void testProlificSporeProducerTrait() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1, "prolific-spore-producer");
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "prolific-spore-producer");

        assertEquals(OrganismType.SPORE, TraitRegistry.offspringType(moss, 0, 0, env));
        assertEquals(OrganismType.SPORE, TraitRegistry.offspringType(root, 0, 0, env));
    }

    @Test
    public void testNoProlificSporeProducerTrait() {
        Environment env = new Environment(50, 50, 50, 50, 50);
        Organism moss = Organism.of("moss-1", OrganismType.MOSS, 10, 1, "quiet");
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1, "quiet");

        assertNotEquals(OrganismType.SPORE, TraitRegistry.offspringType(moss, 0, 0, env));
        assertNotEquals(OrganismType.SPORE, TraitRegistry.offspringType(root, 0, 0, env));
    }
}
