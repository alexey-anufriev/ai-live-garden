package garden.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FunctionalRoleSynergyTest {
    @Test
    public void testMutualistSynergyFungusWithRootNetwork() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 20, 1, "mutualist-synergy");
        Environment env = new Environment(100, 100, 100, 100, 100);
        // fungus needs rootNetworkCount > 0
        int modifier = TraitRegistry.getReproductionThresholdModifier("mutualist-synergy", env, 0, 1L, fungus);
        assertEquals(-3, modifier, "Fungus should get threshold reduction when RootNetwork count > 0");
    }

    @Test
    public void testMutualistSynergyFungusWithoutRootNetwork() {
        Organism fungus = Organism.of("fungus-1", OrganismType.FUNGUS, 20, 1, "mutualist-synergy");
        Environment env = new Environment(100, 100, 100, 100, 100);
        int modifier = TraitRegistry.getReproductionThresholdModifier("mutualist-synergy", env, 0, 0L, fungus);
        assertEquals(0, modifier, "Fungus should not get threshold reduction when RootNetwork count == 0");
    }

    @Test
    public void testMutualistSynergyRootNetworkWithFungus() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 20, 1, "mutualist-synergy");
        Environment env = new Environment(100, 100, 100, 100, 100);
        // root needs fungalContribution > 0
        int modifier = TraitRegistry.getReproductionThresholdModifier("mutualist-synergy", env, 10, 0L, root);
        assertEquals(-3, modifier, "RootNetwork should get threshold reduction when fungalContribution > 0");
    }

    @Test
    public void testMutualistSynergyRootNetworkWithoutFungus() {
        Organism root = Organism.of("root-1", OrganismType.ROOT_NETWORK, 20, 1, "mutualist-synergy");
        Environment env = new Environment(100, 100, 100, 100, 100);
        int modifier = TraitRegistry.getReproductionThresholdModifier("mutualist-synergy", env, 0, 0L, root);
        assertEquals(0, modifier, "RootNetwork should not get threshold reduction when fungalContribution == 0");
    }
}
