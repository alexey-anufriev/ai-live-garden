package garden.ai;

import java.util.List;

public class ContributionCalculator {

    public record ContributionPhaseContext(List<Organism> organisms, Environment environment) {}

    public record ContributionResult(int rootContribution, int fungalContribution, int fungalAttractorContribution, int mossContribution) {}

    public static ContributionResult calculate(ContributionPhaseContext context) {
        long releaserCount = TraitRegistry.count(context.organisms(), "buffer-releaser");
        int rootContribution = calculateRootContribution(context.organisms(), context.environment(), releaserCount);
        int fungalContribution = calculateFungalContribution(context.organisms(), context.environment());
        int fungalAttractorContribution = (TraitRegistry.count(context.organisms(), "fungal-attractor", OrganismType.ROOT_NETWORK) > 0 && fungalContribution > 0) ? 1 : 0;
        int mossContribution = context.organisms().stream().anyMatch(organism -> organism.type() == OrganismType.MOSS) ? 1 : 0;
        return new ContributionResult(rootContribution, fungalContribution, fungalAttractorContribution, mossContribution);
    }

    private static long countType(List<Organism> organisms, OrganismType type) {
        return organisms.stream().filter(o -> o.type() == type).count();
    }

    public static int calculateRootContribution(List<Organism> organisms, Environment environment, long releaserCount) {
        long fungusCount = countType(organisms, OrganismType.FUNGUS);
        long mycelialRootMediatorCount = (fungusCount > 0) ? TraitRegistry.countAnimalTrait(organisms, "mycelial-root-mediator") : 0;

        var context = new RootContributionCalculator.RootContributionContext(
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
        return RootContributionCalculator.calculate(context);
    }

    public static int calculateFungalContribution(List<Organism> organisms, Environment environment) {
        return FungalContributionCalculator.calculate(new FungalContributionCalculator.FungalContributionContext(
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
        ));
    }
}
