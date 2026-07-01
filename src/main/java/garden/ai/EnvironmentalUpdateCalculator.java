package garden.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculator for the environmental update phase of the garden simulation.
 */
public class EnvironmentalUpdateCalculator {

    public record EnvironmentalUpdateContext(
            List<Organism> organisms,
            Environment environment,
            int nextCycle,
            List<GardenEvent> events,
            int rootContribution,
            int fungalContribution
    ) {}

    public record EnvironmentalUpdateResult(Environment nextEnvironment, List<GardenEvent> updatedEvents) {}

    public static EnvironmentalUpdateResult calculate(EnvironmentalUpdateContext context) {
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
        Environment nextEnvironment = context.environment().next(context.nextCycle(), (int) plantCount, (int) animalCount, context.rootContribution(), context.fungalContribution(), plantConsumptionReduction, (int) demandRegulatorCount / 2, (int) (mobilizerCount + fungalNutrientMobilizerCount), (int) releaserCount, (int) siphonCount);
        
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
        
        return new EnvironmentalUpdateResult(nextEnvironment, nextEvents);
    }
}
