package garden.ai;

/**
 * Environmental conditions are intentionally simple and deterministic.
 * Values are normalized to a human-readable 0..100 range.
 */
public record Environment(int light, int moisture, int warmth, int nutrients, int nutrientBuffer) {

    public Environment {
        light = clamp(light);
        moisture = clamp(moisture);
        warmth = clamp(warmth);
        nutrients = clamp(nutrients);
        nutrientBuffer = clamp(nutrientBuffer);
    }

    /**
     * Produces the deterministic environmental drift for one cycle.
     *
     * @param cycle upcoming cycle number
     * @param plantCount number of plants currently competing for nutrients
     * @param animalCount number of animals contributing decay and disturbance
     * @param rootContribution nutrient contribution from root networks
     * @return the next normalized environment
     */
    public Environment next(int cycle, int plantCount, int animalCount, int rootContribution, int fungalContribution, int plantConsumptionReduction, int rootConsumptionReduction, int mobilizerCount, int releaserCount, int acceleratorCount) {
        return next(cycle, plantCount, animalCount, rootContribution, fungalContribution, plantConsumptionReduction, rootConsumptionReduction, mobilizerCount, releaserCount, acceleratorCount, 0);
    }

    public Environment next(int cycle, int plantCount, int animalCount, int rootContribution, int fungalContribution, int plantConsumptionReduction, int rootConsumptionReduction, int mobilizerCount, int releaserCount, int acceleratorCount, int siphonCount) {
        int lightDelta = cycle % 2 == 0 ? 3 : -2;
        int moistureDelta = cycle % 3 == 0 ? 4 : -1;
        int warmthDelta = cycle % 5 == 0 ? -3 : 2;
        int nutrientDelta = 2 + animalCount / 2 - Math.max(0, plantCount / 5 - plantConsumptionReduction - rootConsumptionReduction);
        
        int releaseRate = nutrients < 5 ? 2 : (nutrients < 10 ? 5 : 10);
        // Force buffer release when nutrients are 0
        if (nutrients == 0) {
            releaseRate = 1;
        } else {
            // Reduce release rate if mobilizers, releasers, or accelerators are present (lower rate = higher release)
            releaseRate = Math.max(1, releaseRate - mobilizerCount - releaserCount - acceleratorCount);
            if (nutrientBuffer > 80) releaseRate = Math.max(1, releaseRate / 2);
        }
        int releasedFromBuffer = nutrientBuffer / releaseRate;
        int syphoned = Math.min(nutrientBuffer, siphonCount * 5);
        int newNutrients = nutrients + nutrientDelta + releasedFromBuffer + syphoned;
        int newBuffer = nutrientBuffer + rootContribution + fungalContribution - releasedFromBuffer - syphoned;
        
        return new Environment(light + lightDelta, moisture + moistureDelta, warmth + warmthDelta, newNutrients, newBuffer);
    }

    /**
     * Returns a compact human-facing label for rendering and event text.
     */
    public String mood() {
        if (moisture > 68 && light > 52 && nutrients > 50) {
            return "lush";
        }
        if (warmth < 34) {
            return "cold and watchful";
        }
        if (light < 30) {
            return "dim";
        }
        if (nutrients < 25) {
            return "hungry";
        }
        return "balanced";
    }

    /**
     * Provides a diagnostic insight when the environment is hungry.
     */
    public String diagnostic(int mobilizerCount, int releaserCount, int acceleratorCount) {
        if (nutrients >= 25) {
            return "stable";
        }
        int releaseRate = nutrients < 5 ? 2 : (nutrients < 10 ? 5 : 10);
        releaseRate = Math.max(1, releaseRate - mobilizerCount - releaserCount - acceleratorCount);
        int released = nutrientBuffer / releaseRate;
        if (nutrientBuffer < 10) {
            return "exhausted (low buffer, release=%d, rate=%d, mobilizers=%d, releasers=%d, accelerators=%d)".formatted(released, releaseRate, mobilizerCount, releaserCount, acceleratorCount);
        }
        return "buffer-supported (low nutrients, release=%d, rate=%d, mobilizers=%d, releasers=%d, accelerators=%d)".formatted(released, releaseRate, mobilizerCount, releaserCount, acceleratorCount);
    }

    /**
     * Provides a detailed diagnostic insight when the environment is hungry, including consumption info.
     */
    public String diagnostic(long mossCount, long fernCount, int mossConsumptionReduction, int fernConsumptionReduction, int rootConsumptionReduction, int mobilizerCount, int releaserCount, int acceleratorCount, long blockedPlantCount, long culledPlantCount, long stressResilientPlantCount) {
        if (nutrients >= 25) {
            return "stable";
        }
        int mossConsumption = (int) Math.max(0, mossCount / 5 - mossConsumptionReduction);
        int fernConsumption = (int) Math.max(0, fernCount / 5 - fernConsumptionReduction);
        int consumption = Math.max(0, mossConsumption + fernConsumption - rootConsumptionReduction);
        
        int releaseRate = nutrients < 5 ? 2 : (nutrients < 10 ? 5 : 10);
        releaseRate = Math.max(1, releaseRate - mobilizerCount - releaserCount - acceleratorCount);
        int released = nutrientBuffer / releaseRate;
        int unmetDemand = Math.max(0, consumption - (nutrients + released));
        
        String bufferInfo = (nutrientBuffer < 10) ? "exhausted" : "buffer-supported";
        return "%s (nutrients=%d, buffer=%d, release=%d, consumption=%d [moss=%d, fern=%d], root-reduction=%d, mobilizers=%d, releasers=%d, accelerators=%d, blocked-plants=%d, unmet=%d, culled=%d, stress-resilient=%d)".formatted(
            bufferInfo, nutrients, nutrientBuffer, released, consumption, mossConsumption, fernConsumption, rootConsumptionReduction, mobilizerCount, releaserCount, acceleratorCount, blockedPlantCount, unmetDemand, culledPlantCount, stressResilientPlantCount);
    }

    /**
     * Indicates whether current conditions are favorable enough for ordinary plant growth.
     */
    public boolean favorsPlants() {
        return light >= 45 && moisture >= 45 && nutrients >= 30;
    }

    /**
     * Returns a new environment with updated nutrients, moisture, and buffer based on feeding results.
     */
    public Environment applyFeeding(int nutrientContribution, int predatorContribution, int moistureContribution, int bufferBoost) {
        return new Environment(light,
                               moisture + moistureContribution,
                               warmth,
                               nutrients + nutrientContribution + predatorContribution,
                               nutrientBuffer + bufferBoost);
    }

    /**
     * Returns a new environment with updated nutrients.
     */
    public Environment withNutrients(int bonus) {
        return new Environment(light, moisture, warmth, nutrients + bonus, nutrientBuffer);
    }

    /**
     * Returns a new environment with updated moisture.
     */
    public Environment withMoisture(int bonus) {
        return new Environment(light, moisture + bonus, warmth, nutrients, nutrientBuffer);
    }

    /**
     * Returns a new environment with updated nutrient buffer.
     */
    public Environment withNutrientBuffer(int bonus) {
        return new Environment(light, moisture, warmth, nutrients, bonus);
    }

    private static int clamp(int value) {
        return Math.clamp(value, 0, 100);
    }
}
