package garden.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculator for the environmental update phase of the garden simulation.
 */
public class EnvironmentalUpdateCalculator {

    public record EnvironmentalUpdateResult(Environment nextEnvironment, List<GardenEvent> updatedEvents) {}

    public static EnvironmentalUpdateResult calculate(List<Organism> organisms, Environment environment, int nextCycle, List<GardenEvent> events, int rootContribution, int fungalContribution) {
        List<GardenEvent> nextEvents = new ArrayList<>(events);
        long plantCount = organisms.stream().filter(organism -> organism.type().isPlant()).count();
        long animalCount = organisms.stream().filter(organism -> organism.type().isAnimal()).count();
        
        long mossCount = organisms.stream().filter(o -> o.type() == OrganismType.MOSS).count();
        long fernCount = organisms.stream().filter(o -> o.type() == OrganismType.FERN).count();
        long sporeCount = organisms.stream().filter(o -> o.type() == OrganismType.SPORE).count();
        long rootNetworkCount = organisms.stream().filter(o -> o.type() == OrganismType.ROOT_NETWORK).count();
        long fungusCount = organisms.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
        long mossConserverCount = TraitRegistry.count(organisms, "nutrient-conserver", OrganismType.MOSS);
        long mossScavengerCount = TraitRegistry.count(organisms, "moss-nutrient-scavenger", OrganismType.MOSS);
        long fernConserverCount = TraitRegistry.count(organisms, "nutrient-conserver", OrganismType.FERN);
        long mobilizerCount = TraitRegistry.count(organisms, "nutrient-mobilizer");
        long fungalNutrientMobilizerCount = TraitRegistry.count(organisms, "fungal-nutrient-mobilizer", OrganismType.FUNGUS);
        long releaserCount = TraitRegistry.count(organisms, "buffer-releaser");
        long recyclerCount = TraitRegistry.count(organisms, "nutrient-recycler");
        long distributorCount = TraitRegistry.count(organisms, "nutrient-distributor");
        long demandRegulatorCount = TraitRegistry.count(organisms, "nutrient-demand-regulator");
        long siphonCount = TraitRegistry.count(organisms, "buffer-siphon");
        
        int reductionFactor = environment.nutrients() < 10 ? 1 : 5;
        int plantConsumptionReduction = (int) ((mossConserverCount + mossScavengerCount + fernConserverCount) / reductionFactor);
        Environment nextEnvironment = environment.next(nextCycle, (int) plantCount, (int) animalCount, rootContribution, fungalContribution, plantConsumptionReduction, (int) demandRegulatorCount / 2, (int) (mobilizerCount + fungalNutrientMobilizerCount), (int) releaserCount, (int) siphonCount);
        
        int production = 2 + (int)animalCount / 2;
        int mossConsumption = (int)Math.max(0, mossCount / 5 - (mossConserverCount + mossScavengerCount) / reductionFactor);
        int fernConsumption = (int)Math.max(0, fernCount / 5 - fernConserverCount / reductionFactor);
        int consumption = Math.max(0, mossConsumption + fernConsumption - (int) demandRegulatorCount / 2);
        nextEvents.add(new GardenEvent(nextCycle, "Nutrient change breakdown: prod=%d, cons=%d (moss=%d, fern=%d, root-reduction=%d)".formatted(production, consumption, mossConsumption, fernConsumption, (int) demandRegulatorCount / 2)));
        nextEvents.add(new GardenEvent(nextCycle, "Plant breakdown: moss=%d, fern=%d, spore=%d, roots=%d, fungus=%d".formatted(mossCount, fernCount, sporeCount, rootNetworkCount, fungusCount)));

        if (nextEnvironment.nutrients() < environment.nutrients()) {
            nextEvents.add(new GardenEvent(nextCycle, "Nutrients are depleted by the plant population."));
        }
        if (nextEnvironment.nutrientBuffer() < environment.nutrientBuffer()) {
            nextEvents.add(new GardenEvent(nextCycle, "Nutrients released from the buffer."));
        }
        if (nextEnvironment.nutrientBuffer() > environment.nutrientBuffer()) {
            nextEvents.add(new GardenEvent(nextCycle, "The nutrient buffer is accumulating."));
        }
        int baseReleaseRate = environment.nutrients() < 5 ? 2 : (environment.nutrients() < 10 ? 5 : 10);
        int effectiveReleaseRate = Math.max(1, baseReleaseRate - (int)(mobilizerCount + fungalNutrientMobilizerCount) - (int)releaserCount - (int)recyclerCount - (int)distributorCount);
        int releasedFromBuffer = environment.nutrientBuffer() / effectiveReleaseRate;
        nextEvents.add(new GardenEvent(nextCycle, "Buffer release stats: baseRate=%d, mobilizers=%d, releasers=%d, recyclers=%d, distributors=%d, effectiveRate=%d, released=%d".formatted(baseReleaseRate, mobilizerCount + fungalNutrientMobilizerCount, releaserCount, recyclerCount, distributorCount, effectiveReleaseRate, releasedFromBuffer)));
        
        int availableNutrients = environment.nutrients() + releasedFromBuffer;
        if (consumption > availableNutrients) {
            nextEvents.add(new GardenEvent(nextCycle, "Nutrient scarcity is bottlenecking growth (consumption=%d, available=%d).".formatted(consumption, availableNutrients)));
        }

        if (plantCount > 200 && nextEnvironment.nutrients() < 10) {
            nextEvents.add(new GardenEvent(nextCycle, "High population pressure is straining nutrient reserves."));
        }
        if (nextEnvironment.nutrientBuffer() < 10) {
            nextEvents.add(new GardenEvent(nextCycle, "The nutrient buffer is near exhaustion."));
        }
        
        return new EnvironmentalUpdateResult(nextEnvironment, nextEvents);
    }
}
