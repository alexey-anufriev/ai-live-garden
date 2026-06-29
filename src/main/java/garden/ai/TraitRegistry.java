package garden.ai;

import java.util.Map;

public class TraitRegistry {
    private static final Map<String, Integer> NUTRIENT_VALUES = Map.of(
            "nutrient-hoarder", 5,
            "nutrient-recycler", 3,
            "nutrient-decomposer", 7,
            "nutrient-enricher", 5,
            "nutrient-distributor", 4,
            "nutrient-sharer", 3,
            "nutrient-storer", 6
    );

    public static int getNutrientValueModifier(String trait) {
        return NUTRIENT_VALUES.getOrDefault(trait, 0);
    }
}
