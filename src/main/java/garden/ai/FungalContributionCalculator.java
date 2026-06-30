package garden.ai;

/**
 * Calculates nutrient contributions from fungal organisms based on trait registry counts.
 */
public class FungalContributionCalculator {

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
            long fungalDecomposerMimicCount
    ) {}

    public static int calculate(FungalContributionContext context) {
        int connectorBonus = (context.rootNetworkCount() > 0) ? 6 : 4;
        int synergizerBonus = (context.mycelialSynergizerCount() > 0 && context.fungusCount() > 0) ? 5 : 0;

        return (int) (context.fungusCount() * 2 +
                      context.decomposerCount() * 3 +
                      context.soilEnricherCount() * 5 +
                      context.networkConnectorCount() * connectorBonus +
                      context.fungalSymbioteCount() * 2 +
                      context.fungalAcceleratorCount() * 10 +
                      context.fungalEnhancerCount() * 8 +
                      context.fungalBufferStabilizerCount() * 12 +
                      context.fungalGardenerCount() * 5 +
                      context.fungalFertilizerCount() * 7 +
                      context.fungalDecomposerMimicCount() * 5) + synergizerBonus;
    }
}
