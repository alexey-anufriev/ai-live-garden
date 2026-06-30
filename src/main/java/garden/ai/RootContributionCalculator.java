package garden.ai;

import java.util.List;

/**
 * Calculates nutrient contributions from the root network based on organism traits.
 */
public class RootContributionCalculator {

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

    public static int calculate(RootContributionContext context) {
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
}
