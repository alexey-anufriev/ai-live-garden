package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.ArrayList;

public class BufferReleaseAcceleratorTest {
    @Test
    void acceleratorIncreasesNutrientRelease() {
        // Nutrients=50, buffer=100.
        // releaseRate = 10 (without accelerator).
        // With 1 accelerator: releaseRate = 10 - 1 = 9.
        Environment env = new Environment(50, 50, 50, 50, 100);
        
        // Accelerator organism
        Organism accelerator = Organism.of("root-accelerator", OrganismType.ROOT_NETWORK, 10, 5, "buffer-release-accelerator");
        List<Organism> organisms = List.of(accelerator);
        
        // Without accelerator (empty list of organisms)
        OrganismInteractionCalculator.EnvironmentalDynamicsContext contextNo = new OrganismInteractionCalculator.EnvironmentalDynamicsContext(
            new ArrayList<>(), env, 1, new ArrayList<>()
        );
        OrganismInteractionCalculator.EnvironmentalDynamicsResult resultNo = OrganismInteractionCalculator.calculateEnvironmentalDynamics(contextNo);
        
        // With accelerator
        OrganismInteractionCalculator.EnvironmentalDynamicsContext contextWith = new OrganismInteractionCalculator.EnvironmentalDynamicsContext(
            organisms, env, 1, new ArrayList<>()
        );
        OrganismInteractionCalculator.EnvironmentalDynamicsResult resultWith = OrganismInteractionCalculator.calculateEnvironmentalDynamics(contextWith);

        // Without accelerator: release = 100/10 = 10. nutrients = 50 + 2 - 20 (consumption) + 10 = 42.
        // With accelerator: release = 100/9 = 11. nutrients = 50 + 2 - 20 (consumption) + 11 = 43.
        assertThat(resultWith.nextEnvironment().nutrients()).isGreaterThan(resultNo.nextEnvironment().nutrients());
    }
}
