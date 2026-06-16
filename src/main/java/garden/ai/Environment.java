package garden.ai;

/**
 * Environmental conditions are intentionally simple and deterministic.
 * Values are normalized to a human-readable 0..100 range.
 */
public record Environment(int light, int moisture, int warmth) {

    public Environment {
        light = clamp(light);
        moisture = clamp(moisture);
        warmth = clamp(warmth);
    }

    public Environment next(int cycle) {
        int lightDelta = cycle % 2 == 0 ? 4 : -2;
        int moistureDelta = cycle % 3 == 0 ? 5 : -1;
        int warmthDelta = cycle % 4 == 0 ? -3 : 2;
        return new Environment(light + lightDelta, moisture + moistureDelta, warmth + warmthDelta);
    }

    public String mood() {
        if (moisture > 70 && light > 50) {
            return "lush";
        }
        if (warmth < 35) {
            return "cold and watchful";
        }
        if (light < 30) {
            return "dim";
        }
        return "balanced";
    }

    private static int clamp(int value) {
        return Math.clamp(value, 0, 100);
    }
}
