package garden.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.ArrayList;

public class BufferReleaseAcceleratorTest {
    @Test
    void acceleratorIncreasesNutrientRelease() {
        // Nutrients=50, buffer=50.
        // releaseRate = 10 (without accelerator).
        // With 1 accelerator: releaseRate = 10 - 2 = 8.
        Environment env = new Environment(50, 50, 50, 50, 50);
        
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

        // Without accelerator: release = 50/10 = 5. nutrients = 50 + 2 - 20 (consumption) + 5 = 37.
        // With accelerator: release = 50/8 = 6.25 -> 6. nutrients = 50 + 2 - 20 (consumption) + 6 = 38.
        assertThat(resultWith.nextEnvironment().nutrients()).isGreaterThan(resultNo.nextEnvironment().nutrients());
    }
}
