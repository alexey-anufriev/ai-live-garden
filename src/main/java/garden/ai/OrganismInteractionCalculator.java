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
            ContributionResult contribution,
            Environment nextEnvironment,
            List<GardenEvent> updatedEvents
    ) {}

    public record ContributionResult(int rootContribution, int fungalContribution, int fungalAttractorContribution, int mossContribution) {}

    public record RootContributionContext(
            long rootNetworkCount,
            long nutrientWeaverCount,
            long nutrientSharerCount,
            long bufferOptimizerCount,
            long soilMasterCount,
            long nutrientRecyclerCount,
            long nutrientTranslocatorCount,
            long nutrientSynthesizerCount,
            long nutrientReclaimerCount,
            long nutrientProducerCount,
            long nutrientPumpCount,
            long nutrientDistributorCount,
            long fungalRootSymbiontCount,
            long mycelialRootMediatorCount,
            long releaserCount,
            int nutrients,
            int nutrientBuffer
    ) {}

    public record FungalContributionContext(
            long fungusCount,
            long decomposerCount,
            long soilEnricherCount,
            long networkConnectorCount,
            long fungalSymbioteCount,
            long fungalAcceleratorCount,
            long fungalEnhancerCount,
            long fungalBufferStabilizerCount,
            long fungalGardenerCount,
            long fungalFertilizerCount,
            long rootNetworkCount,
            long mycelialSynergizerCount,
            long fungalDecomposerMimicCount,
            int nutrientBuffer
    ) {}

    public static EnvironmentalDynamicsResult calculateEnvironmentalDynamics(EnvironmentalDynamicsContext context) {
        ContributionResult contribution = calculateContribution(context.organisms(), context.environment());

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

    public static ContributionResult calculateContribution(List<Organism> organisms, Environment environment) {
        long releaserCount = TraitRegistry.count(organisms, "buffer-releaser");
        int rootContribution = calculateRootContribution(organisms, environment, releaserCount);
        int fungalContribution = calculateFungalContribution(organisms, environment);
        int fungalAttractorContribution = (TraitRegistry.count(organisms, "fungal-attractor", OrganismType.ROOT_NETWORK) > 0 && fungalContribution > 0) ? 1 : 0;
        int mossContribution = organisms.stream().anyMatch(organism -> organism.type() == OrganismType.MOSS) ? 1 : 0;
        return new ContributionResult(rootContribution, fungalContribution, fungalAttractorContribution, mossContribution);
    }

    public static int calculateRootContribution(List<Organism> organisms, Environment environment, long releaserCount) {
        long fungusCount = TraitRegistry.count(organisms, OrganismType.FUNGUS);
        long mycelialRootMediatorCount = (fungusCount > 0) ? TraitRegistry.countAnimalTrait(organisms, "mycelial-root-mediator") : 0;

        var context = new RootContributionContext(
                TraitRegistry.count(organisms, OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-weaver", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-sharer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "buffer-optimizer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "soil-master", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-recycler", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-translocator", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-synthesizer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-reclaimer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-producer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-pump", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-distributor", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "fungal-root-symbiont", OrganismType.ROOT_NETWORK),
                mycelialRootMediatorCount,
                releaserCount,
                environment.nutrients(),
                environment.nutrientBuffer()
        );
        return calculateRoot(context);
    }

    private static int calculateRoot(RootContributionContext context) {
        if (context.nutrients() < 5) {
            return (int) (context.rootNetworkCount() * 10 + context.nutrientWeaverCount() * 10 + context.nutrientSharerCount() * 20 + context.bufferOptimizerCount() * 20 + context.soilMasterCount() * 30 + context.nutrientRecyclerCount() * 10 + context.nutrientTranslocatorCount() * 40 + context.nutrientSynthesizerCount() * 30 + context.nutrientReclaimerCount() * 25 + context.nutrientProducerCount() * 50 + context.nutrientPumpCount() * 60 + context.nutrientDistributorCount() * 40 + context.fungalRootSymbiontCount() * 40 + context.mycelialRootMediatorCount() * 20);
        } else if (context.nutrients() < 10) {
            return (int) (context.rootNetworkCount() * 8 + context.nutrientWeaverCount() * 8 + context.nutrientSharerCount() * 16 + context.bufferOptimizerCount() * 16 + context.soilMasterCount() * 24 + context.nutrientRecyclerCount() * 8 + context.nutrientTranslocatorCount() * 32 + context.nutrientSynthesizerCount() * 24 + context.nutrientReclaimerCount() * 20 + context.nutrientProducerCount() * 40 + context.nutrientPumpCount() * 48 + context.nutrientDistributorCount() * 32 + context.fungalRootSymbiontCount() * 32 + context.mycelialRootMediatorCount() * 16);
        } else if (context.nutrients() < 25) {
            return (int) (context.rootNetworkCount() * 4 + context.nutrientWeaverCount() * 4 + context.nutrientSharerCount() * 8 + context.bufferOptimizerCount() * 8 + context.soilMasterCount() * 12 + context.nutrientRecyclerCount() * 4 + context.nutrientTranslocatorCount() * 16 + context.nutrientSynthesizerCount() * 12 + context.nutrientReclaimerCount() * 10 + context.nutrientProducerCount() * 20 + context.nutrientPumpCount() * 24 + context.nutrientDistributorCount() * 16 + context.fungalRootSymbiontCount() * 16 + context.mycelialRootMediatorCount() * 8);
        } else {
            int recyclerBonus = (context.nutrientBuffer() > 50 ? 5 : 2) + (int) Math.min(context.releaserCount(), 10);
            return (int) (Math.max(1, context.rootNetworkCount() / 2) + context.nutrientWeaverCount() + context.nutrientSharerCount() * 2 + context.bufferOptimizerCount() * 2 + context.soilMasterCount() * 4 + context.nutrientRecyclerCount() * recyclerBonus + context.nutrientTranslocatorCount() * 4 + context.nutrientSynthesizerCount() * 3 + context.nutrientReclaimerCount() * 3 + context.nutrientProducerCount() * 5 + context.nutrientPumpCount() * 6 + context.nutrientDistributorCount() * 4 + context.fungalRootSymbiontCount() * 4 + context.mycelialRootMediatorCount() * 2);
        }
    }

    public static int calculateFungalContribution(List<Organism> organisms, Environment environment) {
        var context = new FungalContributionContext(
                TraitRegistry.count(organisms, OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "nutrient-decomposer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungus-soil-enricher", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-network-connector", OrganismType.FUNGUS),
                TraitRegistry.countPlantTrait(organisms, "fungal-symbiote"),
                TraitRegistry.count(organisms, "fungal-accelerator", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-enhancer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-buffer-stabilizer", OrganismType.FUNGUS),
                TraitRegistry.countAnimalTrait(organisms, "fungal-gardener"),
                TraitRegistry.countAnimalTrait(organisms, "fungal-fertilizer"),
                TraitRegistry.count(organisms, OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "mycelial-synergizer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "fungal-decomposer-mimic", OrganismType.ROOT_NETWORK),
                environment.nutrientBuffer()
        );
        return calculateFungal(context);
    }

    private static int calculateFungal(FungalContributionContext context) {
        int connectorBonus = (context.rootNetworkCount() > 0) ? 6 : 4;
        int synergizerBonus = (context.mycelialSynergizerCount() > 0 && context.fungusCount() > 0) ? 5 : 0;
        int bufferBonus = (context.nutrientBuffer() > 20) ? 2 : 1;

        return (int) (context.fungusCount() * 2 * bufferBonus +
                      context.decomposerCount() * 3 * bufferBonus +
                      context.soilEnricherCount() * 5 * bufferBonus +
                      context.networkConnectorCount() * connectorBonus * bufferBonus +
                      context.fungalSymbioteCount() * 2 +
                      context.fungalAcceleratorCount() * 10 +
                      context.fungalEnhancerCount() * 8 +
                      context.fungalBufferStabilizerCount() * 12 +
                      context.fungalGardenerCount() * 5 +
                      context.fungalFertilizerCount() * 7 +
                      context.fungalDecomposerMimicCount() * 5) + synergizerBonus;
    }

    // --- Passive Change / Metabolic Logic ---

    public record PassiveChangeContext(
            Environment environment,
            int cycle,
            List<GardenEvent> events,
            ContributionResult contribution,
            List<Organism> allOrganisms
    ) {}

    public record ContributionContext(int moss, int fungal, int fungalAttractor) {}
    public record MetabolicResult(int metabolism, int energyBonus, List<GardenEvent> events) {}
    public record StressResult(boolean isStressed, int energyLoss, Optional<GardenEvent> event) {}

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
        if (!organism.type().isPlant()) return false;
        boolean isResilient = organism.traits().contains("resilient");
        boolean isDormant = organism.traits().contains("dormancy") && environment.nutrients() < 15;
        boolean isDeepRooting = organism.traits().contains("deep-rooting") && environment.moisture() < 30;
        boolean isStressResilient = organism.traits().contains("stress-resilient");
        boolean isStressAvoidant = organism.traits().contains("stress-avoidance");
        boolean crowded = allOrganisms.stream().filter(o -> o.type().isPlant()).count() > 5000;
        return (!environment.favorsPlants() || crowded) && !isResilient && !isDormant && !isDeepRooting && !isStressResilient && !isStressAvoidant;
    }

    public static boolean isAnimalStarving(Organism organism, Environment environment, List<Organism> allOrganisms) {
        if (!organism.type().isAnimal()) return false;
        boolean isResilient = organism.traits().contains("resilient");
        boolean isDormant = organism.traits().contains("dormancy");
        boolean overcrowded = allOrganisms.stream().filter(o -> o.type().isAnimal()).count() > 2500;
        return ((environment.nutrients() + environment.nutrientBuffer() / 2) < 25 || overcrowded) && !isResilient && !isDormant;
    }

    private static Organism maybeMutate(Organism organism, int cycle, List<GardenEvent> events) {
        if ((organism.energy() + organism.curiosity() + cycle + organism.generation()) % 11 != 0) return organism;
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
            Optional<Integer> preyIndex = findPreyIndex(mutable, hunter, hunterIndex, cycle, environment, events);
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
            FeedingBiteResult biteResult = calculateBite(hunter, prey, environment, cycle, rootNetworkCount);
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

    private record FeedingBiteResult(int biteSize, List<GardenEvent> events) {}

    private static FeedingBiteResult calculateBite(Organism hunter, Organism prey, Environment environment, int cycle, long rootNetworkCount) {
        List<GardenEvent> events = new ArrayList<>();
        int bite = hunter.type() == OrganismType.FOX ? 3 : 2;
        if (hunter.traits().contains("nutrient-finder")) bite += 1;
        if (hunter.traits().contains("scavenger") && environment.nutrients() < 25) bite += 1;
        if (hunter.traits().contains("nutrient-hoarder")) bite += 1;
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("predator-focus")) bite += 1;
        if (hunter.traits().contains("root-tapper") && rootNetworkCount > 0) bite += 1;
        if (hunter.traits().contains("nutrient-refiner")) {
            if (!hunter.traits().contains("stressed") || hunter.traits().contains("starving")) {
                bite += 1;
                if (hunter.traits().contains("starving")) events.add(new GardenEvent(cycle, "%s refined nutrients while starving.".formatted(hunter.id())));
            }
        }
        if (hunter.traits().contains("gentle-feeder")) bite = Math.max(1, bite - 1);
        if (hunter.traits().contains("nutrient-reclaimer") && prey.traits().contains("nutrient-storer")) {
            bite += 2;
            events.add(new GardenEvent(cycle, "%s reclaimed extra nutrients from %s.".formatted(hunter.id(), prey.id())));
        }
        if (hunter.traits().contains("nutrient-harvester")) {
            bite += 1;
            events.add(new GardenEvent(cycle, "%s harvested additional nutrients.".formatted(hunter.id())));
        }
        return new FeedingBiteResult(bite, events);
    }

    private static Optional<Integer> findPreyIndex(List<Organism> organisms, Organism hunter, int hunterIndex, int cycle, Environment environment, List<GardenEvent> events) {
        boolean nutrientScout = hunter.traits().contains("nutrient-scout");
        boolean preyTracker = hunter.traits().contains("prey-tracker");
        boolean resourceTracker = hunter.traits().contains("resource-tracker");
        boolean mycelialNetworkScout = hunter.traits().contains("mycelial-network-scout");
        boolean stealthHunter = hunter.traits().contains("stealth-hunter");

        java.util.function.Predicate<Organism> isValidPrey = candidate -> {
            if (candidate.energy() <= 0 || !hunter.type().canEat(candidate.type())) return false;
            if (!stealthHunter) {
                if (candidate.traits().contains("shadow-stepper") && (candidate.id().hashCode() + cycle) % 2 == 0) return false;
                if (candidate.traits().contains("camouflaged") && (candidate.id().hashCode() + cycle) % 3 == 0) return false;
            }
            return true;
        };

        if (nutrientScout || resourceTracker) {
            for (int i = 0; i < organisms.size(); i++) {
                if (i == hunterIndex) continue;
                Organism candidate = organisms.get(i);
                if (isValidPrey.test(candidate) && candidate.traits().contains("nutrient-hoarder")) return Optional.of(i);
            }
        }
        
        if (mycelialNetworkScout) {
            long fungusCount = organisms.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
            if (fungusCount > 0) {
                for (int i = 0; i < organisms.size(); i++) {
                    if (i == hunterIndex) continue;
                    Organism candidate = organisms.get(i);
                    if (isValidPrey.test(candidate)) {
                        events.add(new GardenEvent(cycle, "%s scouted prey using the fungal network.".formatted(hunter.id())));
                        return Optional.of(i);
                    }
                }
            }
        }

        if (preyTracker) {
            int maxEnergy = -1;
            int bestIndex = -1;
            for (int i = 0; i < organisms.size(); i++) {
                if (i == hunterIndex) continue;
                Organism candidate = organisms.get(i);
                if (isValidPrey.test(candidate) && candidate.energy() > maxEnergy) {
                    maxEnergy = candidate.energy();
                    bestIndex = i;
                }
            }
            if (bestIndex != -1) return Optional.of(bestIndex);
        }

        for (int i = 0; i < organisms.size(); i++) {
            if (i == hunterIndex) continue;
            Organism candidate = organisms.get(i);
            if (isValidPrey.test(candidate)) return Optional.of(i);
        }
        return Optional.empty();
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
            OrganismType childType = organism.type().offspringType(context.cycle(), organism.generation(), context.environment());
            
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
                Organism child = organism.child(childId, childType, TraitRegistry.getMutationTrait(context.cycle(), organism, childType));
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
        if (currentAnimalCount == 0 && currentPlantCount > 200 && !Boolean.getBoolean("disable.emergency.colonization") && random.nextInt(20 - bufferBonus) == 0) {
            OrganismType herbivore = OrganismType.BEETLE;
            String id = herbivore.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + identifier;
            next.add(Organism.of(id, herbivore, 5, 2, "emergency-colonizer"));
            context.events().add(new GardenEvent(context.cycle(), "A new %s arrived to colonize the garden.".formatted(herbivore.displayName())));
            identifier++;
        }

        long herbivoreCount = next.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.HERBIVORE).count();
        long predatorCount = next.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.PREDATOR).count();
        if (herbivoreCount > 0 && predatorCount < 3 && !Boolean.getBoolean("disable.emergency.colonization") && random.nextInt(20 - bufferBonus) == 0) {
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
        if (organism.type().isPlant()) {
            threshold = 14;
        } else if (organism.type() == OrganismType.FOX) {
            threshold = 15;
        }
        for (String trait : organism.traits()) {
            threshold += TraitRegistry.getReproductionThresholdModifier(trait, environment, fungalContribution);
        }
        return threshold;
    }

    public static Garden advance(Garden garden) {
        int nextCycle = garden.cycle() + 1;
        
        EnvironmentalDynamicsResult dynamics = calculateEnvironmentalDynamics(new EnvironmentalDynamicsContext(garden.organisms(), garden.environment(), nextCycle, new ArrayList<>(garden.events())));
        ContributionResult contribution = dynamics.contribution();
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
