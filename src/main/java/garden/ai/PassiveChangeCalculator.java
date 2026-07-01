package garden.ai;

import java.util.List;
import java.util.Optional;

public class PassiveChangeCalculator {

    public static Organism calculate(Organism organism, Environment environment, int cycle, List<GardenEvent> events, ContributionCalculator.ContributionResult contribution, List<Organism> allOrganisms) {
        Organism changed = organism;
        if (organism.type().isPlant()) {
            int growth = environment.favorsPlants() ? 2 : 0;
            if (organism.type() == OrganismType.ROOT_NETWORK && environment.nutrients() > 45) {
                growth += 1;
            }
            if (organism.type() == OrganismType.ROOT_NETWORK && environment.nutrients() < 25) {
                growth += 1;
            }
            if (organism.type() == OrganismType.MOSS && environment.moisture() > 60) {
                growth += 1;
            }
            // Trait-based growth
            for (String trait : organism.traits()) {
                TraitRegistry.PlantGrowthEffect effect = TraitRegistry.getPlantGrowthEffect(trait, cycle, organism, environment, allOrganisms, contribution.fungalContribution());
                if (effect != null) {
                    growth += effect.growthChange();
                    if (effect.event() != null) {
                        events.add(effect.event());
                    }
                }
            }
            if (organism.type() == OrganismType.ROOT_NETWORK && changed.traits().contains("fungal-root-symbiont") && contribution.fungalContribution() > 0) {
                events.add(new GardenEvent(cycle, "%s benefited from its fungal symbiont.".formatted(changed.id())));
            }
            if (organism.type() == OrganismType.SPORE && environment.light() < 45) {
                changed = changed.withCuriosity(changed.curiosity() + 2);
            }
            changed = changed.withEnergy(changed.energy() + growth);
        } else {
            MetabolismCalculator.MetabolicResult result = MetabolismCalculator.calculate(cycle, changed, environment, new MetabolismCalculator.ContributionContext(contribution.mossContribution(), contribution.fungalContribution(), contribution.fungalAttractorContribution()));
            events.addAll(result.events());
            changed = changed.withEnergy(changed.energy() + result.energyBonus() - result.metabolism())
                    .withCuriosity(changed.curiosity() + (cycle % 4 == 0 ? 1 : 0));
        }

        maybeDescribeChange(organism, changed, environment, cycle).ifPresent(events::add);
        
        if (changed.energy() <= 2 && changed.energy() > 0) {
            events.add(new GardenEvent(cycle, "%s is at a critical energy level.".formatted(changed.id())));
        }

        boolean isResilient = changed.traits().contains("resilient");
        boolean isDormant = changed.traits().contains("dormancy") && environment.nutrients() < 15;
        boolean isDeepRooting = changed.traits().contains("deep-rooting") && environment.moisture() < 30;
        boolean isStressResilient = changed.traits().contains("stress-resilient");
        boolean isStressAvoidant = changed.traits().contains("stress-avoidance");

        if (organism.type().isPlant()) {
            if (!environment.favorsPlants() && !isResilient && !isDormant && !isDeepRooting && !isStressResilient && !isStressAvoidant) {
                if (environment.nutrients() == 0) {
                    changed = changed.withEnergy(Math.max(0, changed.energy() - 1));
                    events.add(new GardenEvent(cycle, "%s lost energy due to environmental stress.".formatted(changed.id())));
                }
                changed = changed.withTrait("stressed");
            } else {
                changed = changed.withoutTrait("stressed");
            }
        } else if (organism.type().isAnimal()) {
            if (environment.nutrients() < 25 && !isResilient && !isDormant) {
                changed = changed.withTrait("starving");
            } else {
                changed = changed.withoutTrait("starving");
            }
        }

        return maybeMutate(changed, cycle, events);
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
