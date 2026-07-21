package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class PopulationDynamicsTest {

    @Test
    void moreThanTwoBirthsRemainPossibleWithoutUnboundedGrowth() {
        Environment env = new Environment(100, 100, 100, 100, 100);
        List<Organism> organisms = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            organisms.add(Organism.of("moss-" + i, OrganismType.MOSS, 50, 1));
        }
        
        OrganismInteractionCalculator.PopulationDynamicsContext context = new OrganismInteractionCalculator.PopulationDynamicsContext(
                env, organisms, 1, 10, new ArrayList<>(), 0, new Random());
        
        OrganismInteractionCalculator.PopulationDynamicsResult result = OrganismInteractionCalculator.calculatePopulationDynamics(context);

        assertThat(result.organisms()).hasSize(8);
    }

    @Test
    void dominantPopulationCannotReproduceDuringDensityPressure() {
        Environment environment = new Environment(100, 100, 100, 100, 100);
        List<Organism> organisms = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 50, 1));
        }
        for (int i = 0; i < 20; i++) {
            organisms.add(Organism.of("moss-" + i, OrganismType.MOSS, 50, 1));
        }

        OrganismInteractionCalculator.PopulationDynamicsResult result =
                OrganismInteractionCalculator.calculatePopulationDynamics(
                        new OrganismInteractionCalculator.PopulationDynamicsContext(
                                environment, organisms, 1, 1_000, new ArrayList<>(), 0, new Random(1)));

        assertThat(result.organisms().stream().filter(organism -> organism.type() == OrganismType.BEETLE))
                .hasSize(200);
        assertThat(result.organisms().size() - organisms.size())
                .isBetween(1, OrganismInteractionCalculator.totalBirthBudget(environment));
    }

    @Test
    void functionalRolesReceiveLargerPerTypeBudgets() {
        Environment env = new Environment(100, 100, 100, 100, 100);
        List<Organism> balanced = List.of(
                Organism.of("fox-1", OrganismType.FOX, 10, 1),
                Organism.of("fungus-1", OrganismType.FUNGUS, 10, 1),
                Organism.of("root-1", OrganismType.ROOT_NETWORK, 10, 1),
                Organism.of("moss-1", OrganismType.MOSS, 10, 1));

        assertThat(OrganismInteractionCalculator.typeBirthBudget(OrganismType.FOX, balanced, env)).isEqualTo(6);
        assertThat(OrganismInteractionCalculator.typeBirthBudget(OrganismType.FUNGUS, balanced, env)).isEqualTo(6);
        assertThat(OrganismInteractionCalculator.typeBirthBudget(OrganismType.ROOT_NETWORK, balanced, env)).isEqualTo(6);
        assertThat(OrganismInteractionCalculator.typeBirthBudget(OrganismType.MOSS, balanced, env)).isEqualTo(3);
    }

    @Test
    void beetleReproductionDependsOnBuffer() {
        Environment lowBuffer = new Environment(100, 100, 100, 100, 10);
        Environment medBuffer = new Environment(100, 100, 100, 100, 40);
        Environment highBuffer = new Environment(100, 100, 100, 100, 90);
        List<Organism> organisms = new ArrayList<>();
        // Total population 650, 150 beetles (23%)
        for (int i = 0; i < 150; i++) {
            organisms.add(Organism.of("beetle-" + i, OrganismType.BEETLE, 50, 1));
        }
        for (int i = 0; i < 500; i++) {
            organisms.add(Organism.of("moss-" + i, OrganismType.MOSS, 50, 1));
        }

        assertThat(OrganismInteractionCalculator.typeBirthBudget(OrganismType.BEETLE, organisms, lowBuffer)).isEqualTo(0);
        assertThat(OrganismInteractionCalculator.typeBirthBudget(OrganismType.BEETLE, organisms, medBuffer)).isEqualTo(1);
        assertThat(OrganismInteractionCalculator.typeBirthBudget(OrganismType.BEETLE, organisms, highBuffer)).isEqualTo(3);
    }

    @Test
    void foxBirthBudgetDependsOnPopulation() {
        Environment env = new Environment(100, 100, 100, 100, 100);
        List<Organism> organisms = new ArrayList<>();
        // 3001 foxes
        for (int i = 0; i < 3001; i++) {
            organisms.add(Organism.of("fox-" + i, OrganismType.FOX, 50, 1));
        }
        
        assertThat(OrganismInteractionCalculator.typeBirthBudget(OrganismType.FOX, organisms, env)).isEqualTo(1);
        
        // 2000 foxes
        organisms = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            organisms.add(Organism.of("fox-" + i, OrganismType.FOX, 50, 1));
        }
        
        assertThat(OrganismInteractionCalculator.typeBirthBudget(OrganismType.FOX, organisms, env)).isEqualTo(6);
    }
}
