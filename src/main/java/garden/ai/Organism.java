package garden.ai;

import java.util.Objects;

/**
 * A single living element in the garden.
 *
 * @param id stable identifier used in events and rendering
 * @param type organism kind
 * @param energy rough ability to act in future cycles
 * @param curiosity tendency to respond to environmental change
 */
public record Organism(String id, OrganismType type, int energy, int curiosity) {

    public Organism {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (energy < 0) {
            throw new IllegalArgumentException("energy must not be negative");
        }
        if (curiosity < 0) {
            throw new IllegalArgumentException("curiosity must not be negative");
        }
    }

    public Organism withEnergy(int nextEnergy) {
        return new Organism(id, type, Math.max(0, nextEnergy), curiosity);
    }

    public Organism withCuriosity(int nextCuriosity) {
        return new Organism(id, type, energy, Math.max(0, nextCuriosity));
    }

    public String describe() {
        return "%s %s energy=%d curiosity=%d".formatted(id, type.displayName(), energy, curiosity);
    }
}
