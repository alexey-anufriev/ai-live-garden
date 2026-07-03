package garden.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculator for the environmental and contribution dynamics of the garden simulation.
 */
public class EnvironmentalDynamicsCalculator {

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

    public static EnvironmentalDynamicsResult calculate(EnvironmentalDynamicsContext context) {
        ContributionResult contribution = calculateContribution(context.organisms(), context.environment());

        List<GardenEvent> nextEvents = new ArrayList<>(context.events());
        long plantCount = context.organisms().stream().filter(organism -> organism.type().isPlant()).count();
        long animalCount = context.organisms().stream().filter(organism -> organism.type().isAnimal()).count();
        
        long mossCount = context.organisms().stream().filter(o -> o.type() == OrganismType.MOSS).count();
        long fernCount = context.organisms().stream().filter(o -> o.type() == OrganismType.FERN).count();
        long sporeCount = context.organisms().stream().filter(o -> o.type() == OrganismType.SPORE).count();
        long rootNetworkCount = context.organisms().stream().filter(o -> o.type() == OrganismType.ROOT_NETWORK).count();
        long fungusCount = context.organisms().stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
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

    private static long countType(List<Organism> organisms, OrganismType type) {
        return organisms.stream().filter(o -> o.type() == type).count();
    }

    public static int calculateRootContribution(List<Organism> organisms, Environment environment, long releaserCount) {
        long fungusCount = countType(organisms, OrganismType.FUNGUS);
        long mycelialRootMediatorCount = (fungusCount > 0) ? TraitRegistry.countAnimalTrait(organisms, "mycelial-root-mediator") : 0;

        var context = new RootContributionContext(
                countType(organisms, OrganismType.ROOT_NETWORK),
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
                countType(organisms, OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "nutrient-decomposer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungus-soil-enricher", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-network-connector", OrganismType.FUNGUS),
                TraitRegistry.countPlantTrait(organisms, "fungal-symbiote"),
                TraitRegistry.count(organisms, "fungal-accelerator", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-enhancer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-buffer-stabilizer", OrganismType.FUNGUS),
                TraitRegistry.countAnimalTrait(organisms, "fungal-gardener"),
                TraitRegistry.countAnimalTrait(organisms, "fungal-fertilizer"),
                countType(organisms, OrganismType.ROOT_NETWORK),
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
}
