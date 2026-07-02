package garden.ai;

import java.util.Optional;

/**
 * Calculator for identifying and evaluating environmental stress and starvation conditions.
 */
public class StressCalculator {

    public record StressResult(
            boolean isStressed,
            int energyLoss,
            Optional<GardenEvent> event
    ) {}

    public static StressResult calculatePlantStressResult(Organism organism, Environment environment, int cycle) {
        if (!isPlantStressed(organism, environment)) {
            return new StressResult(false, 0, Optional.empty());
        }

        int energyLoss = 0;
        Optional<GardenEvent> event = Optional.empty();

        if (environment.nutrients() == 0) {
            energyLoss = 1;
            event = Optional.of(new GardenEvent(cycle, "%s lost energy due to environmental stress.".formatted(organism.id())));
        }

        return new StressResult(true, energyLoss, event);
    }

    public static boolean isPlantStressed(Organism organism, Environment environment) {
        if (!organism.type().isPlant()) {
            return false;
        }

        boolean isResilient = organism.traits().contains("resilient");
        boolean isDormant = organism.traits().contains("dormancy") && environment.nutrients() < 15;
        boolean isDeepRooting = organism.traits().contains("deep-rooting") && environment.moisture() < 30;
        boolean isStressResilient = organism.traits().contains("stress-resilient");
        boolean isStressAvoidant = organism.traits().contains("stress-avoidance");

        return !environment.favorsPlants() && !isResilient && !isDormant && !isDeepRooting && !isStressResilient && !isStressAvoidant;
    }

    public static boolean isAnimalStarving(Organism organism, Environment environment) {
        if (!organism.type().isAnimal()) {
            return false;
        }

        boolean isResilient = organism.traits().contains("resilient");
        boolean isDormant = organism.traits().contains("dormancy");

        return (environment.nutrients() + environment.nutrientBuffer() / 2) < 25 && !isResilient && !isDormant;
    }
}
