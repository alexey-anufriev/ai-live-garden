package garden.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import garden.ai.Garden;

/**
 * Consolidated calculator for organism-level interactions: 
 * metabolism, stress, feeding, reproduction, colonization, and environmental contribution.
 */
public class OrganismInteractionCalculator {

    // --- Environmental Dynamics Records & Logic ---

    public record EnvironmentalDynamicsContext(
            List<Organism> organisms,
            Environment environment,
            int nextCycle,
            List<GardenEvent> events
    ) {}

    public record EnvironmentalDynamicsResult(
            TraitRegistry.ContributionResult contribution,
            Environment nextEnvironment,
            List<GardenEvent> updatedEvents
    ) {}





    public static EnvironmentalDynamicsResult calculateEnvironmentalDynamics(EnvironmentalDynamicsContext context) {
        TraitRegistry.ContributionResult contribution = TraitRegistry.calculateContribution(context.organisms(), context.environment());

        List<GardenEvent> nextEvents = new ArrayList<>(context.events());
        long plantCount = context.organisms().stream().filter(organism -> organism.type().isPlant()).count();
        long animalCount = context.organisms().stream().filter(organism -> organism.type().isAnimal()).count();
        
        long mossCount = TraitRegistry.count(context.organisms(), OrganismType.MOSS);
        long fernCount = TraitRegistry.count(context.organisms(), OrganismType.FERN);
        long sporeCount = TraitRegistry.count(context.organisms(), OrganismType.SPORE);
        long rootNetworkCount = TraitRegistry.count(context.organisms(), OrganismType.ROOT_NETWORK);
        long fungusCount = TraitRegistry.count(context.organisms(), OrganismType.FUNGUS);
        long mossConserverCount = TraitRegistry.count(context.organisms(), "nutrient-conserver", OrganismType.MOSS);
        long mossScavengerCount = TraitRegistry.count(context.organisms(), "moss-nutrient-scavenger", OrganismType.MOSS);
        long fernConserverCount = TraitRegistry.count(context.organisms(), "nutrient-conserver", OrganismType.FERN);
        long mobilizerCount = TraitRegistry.count(context.organisms(), "nutrient-mobilizer");
        long fungalNutrientMobilizerCount = TraitRegistry.count(context.organisms(), "fungal-nutrient-mobilizer", OrganismType.FUNGUS);
        long releaserCount = TraitRegistry.count(context.organisms(), "buffer-releaser");
        long recyclerCount = TraitRegistry.count(context.organisms(), "nutrient-recycler");
        long distributorCount = TraitRegistry.count(context.organisms(), "nutrient-distributor");
        long demandRegulatorCount = TraitRegistry.count(context.organisms(), "nutrient-demand-regulator");
        long siphonCount = TraitRegistry.count(context.organisms(), "buffer-siphon");
        
        int reductionFactor = context.environment().nutrients() < 10 ? 1 : 5;
        int plantConsumptionReduction = (int) ((mossConserverCount + mossScavengerCount + fernConserverCount) / reductionFactor);
        Environment nextEnvironment = context.environment().next(context.nextCycle(), (int) plantCount, (int) animalCount, contribution.rootContribution(), contribution.fungalContribution(), plantConsumptionReduction, (int) demandRegulatorCount / 2, (int) (mobilizerCount + fungalNutrientMobilizerCount), (int) releaserCount, (int) siphonCount);
        
        int production = 2 + (int)animalCount / 2;
        int mossConsumption = (int)Math.max(0, mossCount / 5 - (mossConserverCount + mossScavengerCount) / reductionFactor);
        int fernConsumption = (int)Math.max(0, fernCount / 5 - fernConserverCount / reductionFactor);
        int consumption = Math.max(0, mossConsumption + fernConsumption - (int) demandRegulatorCount / 2);
        nextEvents.add(new GardenEvent(context.nextCycle(), "Nutrient change breakdown: prod=%d, cons=%d (moss=%d, fern=%d, root-reduction=%d)".formatted(production, consumption, mossConsumption, fernConsumption, (int) demandRegulatorCount / 2)));
        nextEvents.add(new GardenEvent(context.nextCycle(), "Plant breakdown: moss=%d, fern=%d, spore=%d, roots=%d, fungus=%d".formatted(mossCount, fernCount, sporeCount, rootNetworkCount, fungusCount)));

        if (nextEnvironment.nutrients() < context.environment().nutrients()) {
            nextEvents.add(new GardenEvent(context.nextCycle(), "Nutrients are depleted by the plant population."));
        }
        if (nextEnvironment.nutrientBuffer() < context.environment().nutrientBuffer()) {
            nextEvents.add(new GardenEvent(context.nextCycle(), "Nutrients released from the buffer."));
        }
        if (nextEnvironment.nutrientBuffer() > context.environment().nutrientBuffer()) {
            nextEvents.add(new GardenEvent(context.nextCycle(), "The nutrient buffer is accumulating."));
        }
        int baseReleaseRate = context.environment().nutrients() < 5 ? 2 : (context.environment().nutrients() < 10 ? 5 : 10);
        int effectiveReleaseRate = Math.max(1, baseReleaseRate - (int)(mobilizerCount + fungalNutrientMobilizerCount) - (int)releaserCount - (int)recyclerCount - (int)distributorCount);
        int releasedFromBuffer = context.environment().nutrientBuffer() / effectiveReleaseRate;
        nextEvents.add(new GardenEvent(context.nextCycle(), "Buffer release stats: baseRate=%d, mobilizers=%d, releasers=%d, recyclers=%d, distributors=%d, effectiveRate=%d, released=%d".formatted(baseReleaseRate, mobilizerCount + fungalNutrientMobilizerCount, releaserCount, recyclerCount, distributorCount, effectiveReleaseRate, releasedFromBuffer)));
        
        int availableNutrients = context.environment().nutrients() + releasedFromBuffer;
        if (consumption > availableNutrients) {
            nextEvents.add(new GardenEvent(context.nextCycle(), "Nutrient scarcity is bottlenecking growth (consumption=%d, available=%d).".formatted(consumption, availableNutrients)));
        }

        if (plantCount > 200 && nextEnvironment.nutrients() < 10) {
            nextEvents.add(new GardenEvent(context.nextCycle(), "High population pressure is straining nutrient reserves."));
        }
        if (nextEnvironment.nutrientBuffer() < 10) {
            nextEvents.add(new GardenEvent(context.nextCycle(), "The nutrient buffer is near exhaustion."));
        }
        
        return new EnvironmentalDynamicsResult(contribution, nextEnvironment, nextEvents);
    }









    // --- Passive Change / Metabolic Logic ---

    public record PassiveChangeContext(
            Environment environment,
            int cycle,
            List<GardenEvent> events,
            TraitRegistry.ContributionResult contribution,
            List<Organism> allOrganisms
    ) {}

    public static List<Organism> calculatePassiveChanges(PassiveChangeContext context) {
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
            TraitRegistry.MetabolicEffect result = TraitRegistry.calculateMetabolism(context.cycle(), changed, context.environment(), context.contribution().mossContribution(), context.contribution().fungalContribution(), context.contribution().fungalAttractorContribution());
            if (result.event() != null) {
                context.events().add(result.event());
            }
            changed = changed.withEnergy(changed.energy() + result.energyBonus() - result.metabolismChange())
                    .withCuriosity(changed.curiosity() + (context.cycle() % 4 == 0 ? 1 : 0));
        }

        maybeDescribeChange(organism, changed, context.environment(), context.cycle()).ifPresent(context.events()::add);
        
        if (changed.energy() <= 2 && changed.energy() > 0) {
            context.events().add(new GardenEvent(context.cycle(), "%s is at a critical energy level.".formatted(changed.id())));
        }

        if (organism.type().isPlant()) {
            TraitRegistry.StressResult stress = TraitRegistry.calculatePlantStress(changed, context.environment(), context.cycle(), context.allOrganisms());
            if (stress.isStressed()) {
                changed = changed.withEnergy(Math.max(0, changed.energy() - stress.energyLoss()));
                if (stress.event() != null) context.events().add(stress.event());
                changed = changed.withTrait("stressed");
            } else {
                changed = changed.withoutTrait("stressed");
            }
        } else if (organism.type().isAnimal()) {
            if (TraitRegistry.isAnimalStarving(changed, context.environment(), context.allOrganisms())) {
                changed = changed.withTrait("starving");
            } else {
                changed = changed.withoutTrait("starving");
            }
        }

        return maybeMutate(changed, context.cycle(), context.events(), context.environment());
    }

    private static Organism maybeMutate(Organism organism, int cycle, List<GardenEvent> events, Environment environment) {
        if ((organism.energy() + organism.curiosity() + cycle + organism.generation()) % 11 != 0) return organism;
        String trait;
        if (organism.type() == OrganismType.ROOT_NETWORK && organism.traits().contains("stressed") && (organism.id().hashCode() + cycle) % 5 == 0) {
            trait = "fungal-symbiote";
        } else if ((organism.traits().contains("stressed") || organism.traits().contains("starving")) && Math.random() < 0.4) {
            if (organism.type() == OrganismType.FOX || organism.type() == OrganismType.FUNGUS || organism.type() == OrganismType.ROOT_NETWORK) {
                trait = "metabolic-resilience";
            } else {
                String[] resilienceTraits = {"hardy", "dormancy", "metabolic-resilience"};
                trait = resilienceTraits[Math.floorMod(cycle + organism.generation(), resilienceTraits.length)];
            }
        } else {
            trait = TraitRegistry.getMutationTrait(cycle, organism, organism.type(), environment);
        }
        Organism changed = organism.withTrait(trait).withCuriosity(organism.curiosity() + 1);
        events.add(new GardenEvent(cycle, "%s adapted a %s trait.".formatted(organism.id(), trait)));
        return changed;
    }

    private static Optional<GardenEvent> maybeDescribeChange(Organism before, Organism after, Environment environment, int cycle) {
        if (after.energy() > before.energy()) return Optional.of(new GardenEvent(cycle, "%s gathered energy from the %s garden.".formatted(after.id(), environment.mood())));
        if (after.energy() < before.energy() && after.type().isAnimal()) return Optional.of(new GardenEvent(cycle, "%s spent energy moving through the garden.".formatted(after.id())));
        if (after.curiosity() > before.curiosity()) return Optional.of(new GardenEvent(cycle, "%s became more curious under changing conditions.".formatted(after.id())));
        return Optional.empty();
    }

    // --- Feeding Phase Logic ---

    public record FeedingResult(List<Organism> organisms, int totalNutrientContribution, int totalMoistureContribution, int nutrientBufferBoost, int culledPlantCount, int predatorNutrientContribution) {}
    public record FeedingPhaseContext(List<Organism> organisms, Environment environment, int cycle, List<GardenEvent> events) {}

    public static FeedingResult calculateFeeding(FeedingPhaseContext context) {
        List<Organism> organisms = context.organisms();
        Environment environment = context.environment();
        int cycle = context.cycle();
        List<GardenEvent> events = context.events();
        List<Organism> mutable = new ArrayList<>(organisms);
        mutable.sort(Comparator.comparing((Organism organism) -> organism.type().kingdom().ordinal())
                .thenComparing(Organism::id));

        int predatorNutrientContribution = 0;
        long rootNetworkCount = organisms.stream().filter(o -> o.type() == OrganismType.ROOT_NETWORK).count();
        
        for (int hunterIndex = 0; hunterIndex < mutable.size(); hunterIndex++) {
            Organism hunter = mutable.get(hunterIndex);
            if (!hunter.type().isAnimal() || hunter.energy() <= 0) continue;
            if (hunter.traits().contains("cautious-feeder") && hunter.energy() > 15) {
                events.add(new GardenEvent(cycle, "%s skipped feeding to conserve energy.".formatted(hunter.id())));
                continue;
            }
            Optional<Integer> preyIndex = TraitRegistry.findPreyIndex(mutable, hunter, hunterIndex, cycle, environment, events);
            if (preyIndex.isEmpty()) {
                if (hunter.traits().contains("starving")) {
                    events.add(new GardenEvent(cycle, "%s is desperately searching for prey in the scarce garden.".formatted(hunter.id())));
                } else if (environment.nutrients() < 25 && (hunter.id().hashCode() + cycle) % 10 == 0) {
                    events.add(new GardenEvent(cycle, "In the hungry garden, %s could not find prey (looking for: %s).".formatted(hunter.id(), hunter.type().prey())));
                }
                continue;
            }

            int index = preyIndex.get();
            Organism prey = mutable.get(index);
            TraitRegistry.BiteEffect biteResult = TraitRegistry.calculateBite(hunter, prey, environment, cycle, rootNetworkCount, mutable);
            int bite = biteResult.biteSize();
            events.addAll(biteResult.events());
            
            if (hunter.type() == OrganismType.FOX) predatorNutrientContribution += 1;
            Organism fedHunter = hunter.withEnergy(hunter.energy() + bite).withTrait("fed-" + cycle);
            Organism weakenedPrey = prey.withEnergy(prey.energy() - bite);
            mutable.set(hunterIndex, fedHunter);
            mutable.set(index, weakenedPrey);
            events.add(new GardenEvent(cycle, "%s fed on %s.".formatted(hunter.id(), prey.id())));
        }

        int totalNutrientContribution = 0;
        int totalMoistureContribution = 0;
        int deadOrganisms = 0;
        int moistureRetainers = 0;
        int mycelialDistributors = 0;
        List<Organism> survivors = new ArrayList<>();
        for (Organism organism : mutable) {
            if (organism.energy() > 0) survivors.add(organism);
            else {
                if (organism.traits().contains("stressed")) {
                    events.add(new GardenEvent(cycle, "%s was culled due to chronic environmental stress.".formatted(organism.id())));
                    if (organism.traits().contains("nutrient-recycler")) totalNutrientContribution += 5;
                }
                totalNutrientContribution += organism.nutrientValue();
                deadOrganisms++;
                if (organism.traits().contains("moisture-retainer") && organism.type() == OrganismType.MOSS) {
                    totalMoistureContribution += 5;
                    moistureRetainers++;
                }
                if (organism.traits().contains("mycelial-distributor")) mycelialDistributors++;
            }
        }
        if (deadOrganisms > 0) {
            events.add(new GardenEvent(cycle, "%d organisms returned %d nutrients to the soil.".formatted(deadOrganisms, totalNutrientContribution)));
            if (moistureRetainers > 0) events.add(new GardenEvent(cycle, "%d moisture-retainers returned %d moisture to the soil.".formatted(moistureRetainers, totalMoistureContribution)));
            if (mycelialDistributors > 0) events.add(new GardenEvent(cycle, "%d mycelial-distributors distributed nutrients to the fungal network.".formatted(mycelialDistributors)));
        }
        survivors.sort(Comparator.comparing(Organism::id));
        
        int bufferBoost = 0;
        for (Organism organism : mutable) {
            if (organism.energy() <= 0 && organism.traits().contains("mycelial-distributor")) {
                long fungusCount = organisms.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
                if (fungusCount > 0) bufferBoost += 5;
            }
            if (organism.energy() > 0 && organism.traits().contains("buffer-stabilizer")) {
                long fungusCount = organisms.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
                if (fungusCount > 0) bufferBoost += 2;
            }
        }
        return new FeedingResult(survivors, totalNutrientContribution, totalMoistureContribution, bufferBoost, deadOrganisms, predatorNutrientContribution);
    }


    // --- Population Dynamics Logic ---

    public record PopulationDynamicsContext(
            Environment environment,
            List<Organism> organisms,
            int cycle,
            int nextId,
            List<GardenEvent> events,
            int fungalContribution,
            Random random
    ) {}

    public record PopulationDynamicsResult(List<Organism> organisms, int nextId) {}

    public static PopulationDynamicsResult calculatePopulationDynamics(PopulationDynamicsContext context) {
        List<Organism> next = new ArrayList<>(context.organisms());
        int identifier = context.nextId();
        int birthsThisCycle = 0;
        long fungusCount = next.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();

        // Reproduction phase
        List<Organism> afterReproduction = new ArrayList<>();
        for (Organism organism : context.organisms()) {
            OrganismType childType = TraitRegistry.offspringType(organism, context.cycle(), organism.generation(), context.environment());
            
            // Fungal role rescue mechanism
            if (fungusCount == 0 && organism.type() == OrganismType.ROOT_NETWORK) {
                childType = OrganismType.FUNGUS;
            }

            boolean isFungalSuccession = (organism.type() == OrganismType.ROOT_NETWORK && childType == OrganismType.FUNGUS);
            boolean canReproduce = (organism.energy() >= reproductionThreshold(organism, context.environment(), context.fungalContribution()) || (isFungalSuccession && organism.energy() >= 4)) && (birthsThisCycle < 2 || isFungalSuccession);

            if (organism.traits().contains("stressed") && !organism.traits().contains("fungal-symbiote") && !isFungalSuccession) {
                canReproduce = false;
            }
            if (organism.traits().contains("starving") && !organism.traits().contains("resourceful-breeder")) {
                canReproduce = false;
            }
            if (organism.traits().contains("cautious-breeder") && context.environment().nutrients() < 10) {
                canReproduce = false;
            }

            if (canReproduce) {
                String childId = childType.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + identifier;
                Organism parentAfterBirth = organism.withEnergy(organism.energy() / 2);
                Organism child = organism.child(childId, childType, TraitRegistry.getMutationTrait(context.cycle(), organism, childType, context.environment()));
                afterReproduction.add(parentAfterBirth);
                afterReproduction.add(child);
                context.events().add(new GardenEvent(context.cycle(), "%s released %s as a new %s.".formatted(
                        organism.id(), child.id(), childType.displayName())));
                identifier++;
                birthsThisCycle++;
            } else {
                afterReproduction.add(organism);
            }
        }
        afterReproduction.sort(Comparator.comparing(Organism::id));
        next = afterReproduction;

        // Colonization phase
        int bufferBonus = context.environment().nutrientBuffer() / 10;
        Random random = context.random() != null ? context.random() : new Random();

        // Spore dispersal adaptor: boost colonization chance
        long sporeDispersalAdaptorCount = next.stream().filter(o -> o.type() == OrganismType.SPORE && o.traits().contains("spore-dispersal-adaptor")).count();
        int dispersalBonus = (int) Math.min(sporeDispersalAdaptorCount * 2, 10);
        int colonizationChance = 20 - bufferBonus - dispersalBonus;
        if (colonizationChance < 2) colonizationChance = 2;

        if (next.isEmpty()) {
            OrganismType[] plantTypes = {OrganismType.MOSS, OrganismType.SPORE, OrganismType.FERN, OrganismType.FUNGUS};
            OrganismType selected = plantTypes[random.nextInt(plantTypes.length)];
            String id = selected.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + identifier;
            next.add(Organism.of(id, selected, 5, 1, "emergency-seed"));
            context.events().add(new GardenEvent(context.cycle(), "A last emergency %s appeared to keep the garden alive.".formatted(selected.displayName())));
            identifier++;
        }

        long currentAnimalCount = next.stream().filter(o -> o.type().isAnimal()).count();
        long currentPlantCount = next.stream().filter(o -> o.type().isPlant()).count();
        if (currentAnimalCount == 0 && currentPlantCount > 200 && !Boolean.getBoolean("disable.emergency.colonization") && random.nextInt(colonizationChance) == 0) {
            OrganismType herbivore = OrganismType.BEETLE;
            String id = herbivore.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + identifier;
            next.add(Organism.of(id, herbivore, 5, 2, "emergency-colonizer"));
            context.events().add(new GardenEvent(context.cycle(), "A new %s arrived to colonize the garden.".formatted(herbivore.displayName())));
            identifier++;
        }

        long herbivoreCount = next.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.HERBIVORE).count();
        long predatorCount = next.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.PREDATOR).count();
        if (herbivoreCount > 0 && predatorCount < 3 && !Boolean.getBoolean("disable.emergency.colonization") && random.nextInt(colonizationChance) == 0) {
            OrganismType predator = OrganismType.FOX;
            String id = predator.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + identifier;
            next.add(Organism.of(id, predator, 5, 8, "emergency-colonizer"));
            context.events().add(new GardenEvent(context.cycle(), "A new %s arrived to colonize the garden.".formatted(predator.displayName())));
            identifier++;
        }

        return new PopulationDynamicsResult(next, identifier);
    }

    public static int reproductionThreshold(Organism organism, Environment environment, int fungalContribution) {
        int threshold = 15;
        if (organism.type() == OrganismType.FUNGUS) {
            threshold = 12;
            if (environment.nutrientBuffer() > 50) {
                threshold -= 2;
            }
        } else if (organism.type().isPlant()) {
            threshold = 14;
        } else if (organism.type() == OrganismType.FOX) {
            threshold = 15;
        }
        if (environment.nutrients() < 25) {
            threshold += 5;
        } else if (environment.nutrients() > 60) {
            if (organism.type() == OrganismType.FOX || organism.type() == OrganismType.FUNGUS || organism.type() == OrganismType.ROOT_NETWORK) {
                threshold -= 2;
            }
        }
        if (organism.type() == OrganismType.ROOT_NETWORK && environment.nutrientBuffer() > 50 && organism.traits().contains("buffer-optimizer")) {
            threshold -= 3;
        }
        for (String trait : organism.traits()) {
            threshold += TraitRegistry.getReproductionThresholdModifier(trait, environment, fungalContribution, organism);
        }
        return threshold;
    }

    public static Garden advance(Garden garden) {
        int nextCycle = garden.cycle() + 1;
        
        EnvironmentalDynamicsResult dynamics = calculateEnvironmentalDynamics(new EnvironmentalDynamicsContext(garden.organisms(), garden.environment(), nextCycle, new ArrayList<>(garden.events())));
        TraitRegistry.ContributionResult contribution = dynamics.contribution();
        Environment nextEnvironment = dynamics.nextEnvironment();
        List<GardenEvent> nextEvents = dynamics.updatedEvents();

        List<Organism> changed = calculatePassiveChanges(new PassiveChangeContext(garden.environment(), nextCycle, nextEvents, contribution, garden.organisms()));

        FeedingResult feeding = calculateFeeding(new FeedingPhaseContext(changed, garden.environment(), nextCycle, nextEvents));
        
        // Add nutrients and moisture based on deaths
        Environment environmentWithNutrientsAndMoisture = nextEnvironment.applyFeeding(
                feeding.totalNutrientContribution(),
                feeding.predatorNutrientContribution(),
                feeding.totalMoistureContribution(),
                feeding.nutrientBufferBoost());
        
        PopulationDynamicsResult population = calculatePopulationDynamics(new PopulationDynamicsContext(
                garden.environment(), feeding.organisms(), nextCycle, garden.nextId(), nextEvents, contribution.fungalContribution(), new Random()));
        List<Organism> finalChanged = population.organisms();
        int nextIdentifier = population.nextId();
        
        nextEvents.add(new GardenEvent(nextCycle,
                "The garden becomes %s after cycle %d.".formatted(environmentWithNutrientsAndMoisture.mood(), nextCycle)));
        
        return new Garden(nextCycle, nextIdentifier, environmentWithNutrientsAndMoisture, finalChanged, nextEvents);
    }
}
