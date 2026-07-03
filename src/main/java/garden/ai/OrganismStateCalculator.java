package garden.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Central calculator for organism energy adjustments, metabolism, stress, and starvation logic.
 */
public class OrganismStateCalculator {

    // Metabolism records
    public record ContributionContext(int moss, int fungal, int fungalAttractor) {}
    public record MetabolicResult(int metabolism, int energyBonus, List<GardenEvent> events) {}

    // Stress records
    public record StressResult(
            boolean isStressed,
            int energyLoss,
            Optional<GardenEvent> event
    ) {}

    // Metabolism logic
    public static MetabolicResult calculateMetabolism(int cycle, Organism organism, Environment environment, ContributionContext contributions) {
        int metabolism = organism.type().metabolism();
        int energyBonus = 0;
        List<GardenEvent> events = new ArrayList<>();

        for (String trait : organism.traits()) {
            TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect(trait, cycle, organism, environment, contributions.fungal(), contributions.moss());
            if (effect != null) {
                metabolism = Math.max(0, metabolism + effect.metabolismChange());
                energyBonus += effect.energyBonus();
                if (effect.event() != null) {
                    events.add(effect.event());
                }
            }
        }

        if (contributions.fungalAttractor() > 0) {
            energyBonus += 1;
            events.add(new GardenEvent(cycle, "%s was attracted to a fungal-rich area.".formatted(organism.id())));
        }
        return new MetabolicResult(metabolism, energyBonus, events);
    }

    // Stress/Starvation logic
    public static StressResult calculatePlantStressResult(Organism organism, Environment environment, int cycle, List<Organism> allOrganisms) {
        if (!isPlantStressed(organism, environment, allOrganisms)) {
            return new StressResult(false, 0, Optional.empty());
        }

        int energyLoss = 0;
        Optional<GardenEvent> event = Optional.empty();

        if (environment.nutrients() == 0) {
            energyLoss = 1;
            event = Optional.of(new GardenEvent(cycle, "%s lost energy due to environmental stress.".formatted(organism.id())));
        } else if (allOrganisms.stream().filter(o -> o.type().isPlant()).count() > 5000) {
            energyLoss = 1;
            event = Optional.of(new GardenEvent(cycle, "%s lost energy due to overcrowding.".formatted(organism.id())));
        }

        return new StressResult(true, energyLoss, event);
    }

    public static boolean isPlantStressed(Organism organism, Environment environment, List<Organism> allOrganisms) {
        if (!organism.type().isPlant()) {
            return false;
        }

        boolean isResilient = organism.traits().contains("resilient");
        boolean isDormant = organism.traits().contains("dormancy") && environment.nutrients() < 15;
        boolean isDeepRooting = organism.traits().contains("deep-rooting") && environment.moisture() < 30;
        boolean isStressResilient = organism.traits().contains("stress-resilient");
        boolean isStressAvoidant = organism.traits().contains("stress-avoidance");

        boolean crowded = allOrganisms.stream().filter(o -> o.type().isPlant()).count() > 5000;

        return (!environment.favorsPlants() || crowded) && !isResilient && !isDormant && !isDeepRooting && !isStressResilient && !isStressAvoidant;
    }

    public static boolean isAnimalStarving(Organism organism, Environment environment, List<Organism> allOrganisms) {
        if (!organism.type().isAnimal()) {
            return false;
        }

        boolean isResilient = organism.traits().contains("resilient");
        boolean isDormant = organism.traits().contains("dormancy");
        boolean overcrowded = allOrganisms.stream().filter(o -> o.type().isAnimal()).count() > 2500;

        return ((environment.nutrients() + environment.nutrientBuffer() / 2) < 25 || overcrowded) && !isResilient && !isDormant;
    }
}
