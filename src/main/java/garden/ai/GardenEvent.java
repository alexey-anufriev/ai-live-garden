package garden.ai;

import java.util.Objects;

/**
 * An observable trace of a garden cycle.
 */
public record GardenEvent(int cycle, String description) {

    public GardenEvent {
        if (cycle < 0) {
            throw new IllegalArgumentException("cycle must not be negative");
        }

        Objects.requireNonNull(description, "description");

        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
    }
}
