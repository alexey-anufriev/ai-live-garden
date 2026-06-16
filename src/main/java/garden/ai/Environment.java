package garden.ai;

/**
 * Environmental conditions are intentionally simple and deterministic.
 * Values are normalized to a human-readable 0..100 range.
 */
public record Environment(int light, int moisture, int warmth, int nutrients) {

    public Environment {
        light = clamp(light);
        moisture = clamp(moisture);
        warmth = clamp(warmth);
        nutrients = clamp(nutrients);
    }

    public Environment next(int cycle, int plantCount, int animalCount) {
        int lightDelta = cycle % 2 == 0 ? 3 : -2;
        int moistureDelta = cycle % 3 == 0 ? 4 : -1;
        int warmthDelta = cycle % 5 == 0 ? -3 : 2;
        int nutrientDelta = 1 + animalCount / 3 - plantCount / 4;
        return new Environment(light + lightDelta, moisture + moistureDelta, warmth + warmthDelta, nutrients + nutrientDelta);
    }

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

    public boolean favorsPlants() {
        return light >= 45 && moisture >= 45 && nutrients >= 35;
    }

    private static int clamp(int value) {
        return Math.clamp(value, 0, 100);
    }
}
