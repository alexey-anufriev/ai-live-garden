package garden.ai;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PassiveChangeCalculator {

    public record PassiveChangeContext(
            Environment environment,
            int cycle,
            List<GardenEvent> events,
            ContributionCalculator.ContributionResult contribution,
            List<Organism> allOrganisms
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
            MetabolismCalculator.MetabolicResult result = MetabolismCalculator.calculate(context.cycle(), changed, context.environment(), new MetabolismCalculator.ContributionContext(context.contribution().mossContribution(), context.contribution().fungalContribution(), context.contribution().fungalAttractorContribution()));
            context.events().addAll(result.events());
            changed = changed.withEnergy(changed.energy() + result.energyBonus() - result.metabolism())
                    .withCuriosity(changed.curiosity() + (context.cycle() % 4 == 0 ? 1 : 0));
        }

        maybeDescribeChange(organism, changed, context.environment(), context.cycle()).ifPresent(context.events()::add);
        
        if (changed.energy() <= 2 && changed.energy() > 0) {
            context.events().add(new GardenEvent(context.cycle(), "%s is at a critical energy level.".formatted(changed.id())));
        }

        boolean isResilient = changed.traits().contains("resilient");
        boolean isDormant = changed.traits().contains("dormancy") && context.environment().nutrients() < 15;
        boolean isDeepRooting = changed.traits().contains("deep-rooting") && context.environment().moisture() < 30;
        boolean isStressResilient = changed.traits().contains("stress-resilient");
        boolean isStressAvoidant = changed.traits().contains("stress-avoidance");

        if (organism.type().isPlant()) {
            if (!context.environment().favorsPlants() && !isResilient && !isDormant && !isDeepRooting && !isStressResilient && !isStressAvoidant) {
                if (context.environment().nutrients() == 0) {
                    changed = changed.withEnergy(Math.max(0, changed.energy() - 1));
                    context.events().add(new GardenEvent(context.cycle(), "%s lost energy due to environmental stress.".formatted(changed.id())));
                }
                changed = changed.withTrait("stressed");
            } else {
                changed = changed.withoutTrait("stressed");
            }
        } else if (organism.type().isAnimal()) {
            if (context.environment().nutrients() < 25 && !isResilient && !isDormant) {
                changed = changed.withTrait("starving");
            } else {
                changed = changed.withoutTrait("starving");
            }
        }

        return maybeMutate(changed, context.cycle(), context.events());
    }


    private static Organism maybeMutate(Organism organism, int cycle, List<GardenEvent> events) {
        if ((organism.energy() + organism.curiosity() + cycle + organism.generation()) % 11 != 0) {
            return organism;
        }
        String trait;
        if (organism.type() == OrganismType.ROOT_NETWORK && organism.traits().contains("stressed") && (organism.id().hashCode() + cycle) % 5 == 0) {
            trait = "fungal-symbiote";
        } else {
            trait = TraitRegistry.getMutationTrait(cycle, organism);
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
