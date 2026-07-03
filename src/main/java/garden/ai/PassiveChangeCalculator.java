package garden.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PassiveChangeCalculator {

    public record PassiveChangeContext(
            Environment environment,
            int cycle,
            List<GardenEvent> events,
            EnvironmentalDynamicsCalculator.ContributionResult contribution,
            List<Organism> allOrganisms
    ) {}

    // State calculation records
    public record ContributionContext(int moss, int fungal, int fungalAttractor) {}
    public record MetabolicResult(int metabolism, int energyBonus, List<GardenEvent> events) {}
    public record StressResult(
            boolean isStressed,
            int energyLoss,
            Optional<GardenEvent> event
    ) {}

    public static List<Organism> calculate(PassiveChangeContext context) {
        return context.allOrganisms().stream()
                .map(organism -> calculateSingle(organism, context))
                .collect(Collectors.toList());
    }

    private static Organism calculateSingle(Organism organism, PassiveChangeContext context) {
        Organism changed = organism;
        if (organism.type().isPlant()) {
            int growth = context.environment().favorsPlants() ? 2 : 0;
            if (organism.type() == OrganismType.ROOT_NETWORK && context.environment().nutrients() > 45) {
                growth += 1;
            }
            if (organism.type() == OrganismType.ROOT_NETWORK && context.environment().nutrients() < 25) {
                growth += 1;
            }
            if (organism.type() == OrganismType.MOSS && context.environment().moisture() > 60) {
                growth += 1;
            }
            // Trait-based growth
            for (String trait : organism.traits()) {
                TraitRegistry.PlantGrowthEffect effect = TraitRegistry.getPlantGrowthEffect(trait, context.cycle(), organism, context.environment(), context.allOrganisms(), context.contribution().fungalContribution());
                if (effect != null) {
                    growth += effect.growthChange();
                    if (effect.event() != null) {
                        context.events().add(effect.event());
                    }
                }
            }
            if (organism.type() == OrganismType.ROOT_NETWORK && changed.traits().contains("fungal-root-symbiont") && context.contribution().fungalContribution() > 0) {
                context.events().add(new GardenEvent(context.cycle(), "%s benefited from its fungal symbiont.".formatted(changed.id())));
            }
            if (organism.type() == OrganismType.SPORE && context.environment().light() < 45) {
                changed = changed.withCuriosity(changed.curiosity() + 2);
            }
            changed = changed.withEnergy(changed.energy() + growth);
        } else {
            MetabolicResult result = calculateMetabolism(context.cycle(), changed, context.environment(), new ContributionContext(context.contribution().mossContribution(), context.contribution().fungalContribution(), context.contribution().fungalAttractorContribution()));
            context.events().addAll(result.events());
            changed = changed.withEnergy(changed.energy() + result.energyBonus() - result.metabolism())
                    .withCuriosity(changed.curiosity() + (context.cycle() % 4 == 0 ? 1 : 0));
        }

        maybeDescribeChange(organism, changed, context.environment(), context.cycle()).ifPresent(context.events()::add);
        
        if (changed.energy() <= 2 && changed.energy() > 0) {
            context.events().add(new GardenEvent(context.cycle(), "%s is at a critical energy level.".formatted(changed.id())));
        }

        if (organism.type().isPlant()) {
            StressResult stress = calculatePlantStressResult(changed, context.environment(), context.cycle(), context.allOrganisms());
            if (stress.isStressed()) {
                changed = changed.withEnergy(Math.max(0, changed.energy() - stress.energyLoss()));
                stress.event().ifPresent(context.events()::add);
                changed = changed.withTrait("stressed");
            } else {
                changed = changed.withoutTrait("stressed");
            }
        } else if (organism.type().isAnimal()) {
            if (isAnimalStarving(changed, context.environment(), context.allOrganisms())) {
                changed = changed.withTrait("starving");
            } else {
                changed = changed.withoutTrait("starving");
            }
        }

        return maybeMutate(changed, context.cycle(), context.events());
    }

    // --- State Logic (moved from OrganismStateCalculator) ---

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



    private static Organism maybeMutate(Organism organism, int cycle, List<GardenEvent> events) {
        if ((organism.energy() + organism.curiosity() + cycle + organism.generation()) % 11 != 0) {
            return organism;
        }
        String trait;
        if (organism.type() == OrganismType.ROOT_NETWORK && organism.traits().contains("stressed") && (organism.id().hashCode() + cycle) % 5 == 0) {
            trait = "fungal-symbiote";
        } else {
            trait = TraitRegistry.getMutationTrait(cycle, organism, organism.type());
        }
        Organism changed = organism.withTrait(trait).withCuriosity(organism.curiosity() + 1);
        events.add(new GardenEvent(cycle, "%s adapted a %s trait.".formatted(organism.id(), trait)));
        return changed;
    }

    private static Optional<GardenEvent> maybeDescribeChange(Organism before, Organism after, Environment environment, int cycle) {
        if (after.energy() > before.energy()) {
            return Optional.of(new GardenEvent(cycle, "%s gathered energy from the %s garden.".formatted(after.id(), environment.mood())));
        }
        if (after.energy() < before.energy() && after.type().isAnimal()) {
            return Optional.of(new GardenEvent(cycle, "%s spent energy moving through the garden.".formatted(after.id())));
        }
        if (after.curiosity() > before.curiosity()) {
            return Optional.of(new GardenEvent(cycle, "%s became more curious under changing conditions.".formatted(after.id())));
        }
        return Optional.empty();
    }
}
