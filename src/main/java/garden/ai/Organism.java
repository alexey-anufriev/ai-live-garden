package garden.ai;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A single living element in the garden.
 *
 * @param id stable identifier used in events and rendering
 * @param type organism kind
 * @param energy rough ability to act in future cycles
 * @param curiosity tendency to respond to environmental change
 * @param generation lineage depth after reproduction or mutation
 * @param traits compact organism memory visible in git diffs
 */
public record Organism(String id, OrganismType type, int energy, int curiosity, int generation, List<String> traits) {

    public Organism {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        traits = List.copyOf(Objects.requireNonNull(traits, "traits"));
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (energy < 0) {
            throw new IllegalArgumentException("energy must not be negative");
        }
        if (curiosity < 0) {
            throw new IllegalArgumentException("curiosity must not be negative");
        }
        if (generation < 0) {
            throw new IllegalArgumentException("generation must not be negative");
        }
    }

    /**
     * Creates a generation-zero organism with one or more starting traits.
     */
    public static Organism of(String id, OrganismType type, int energy, int curiosity, String... traits) {
        return new Organism(id, type, energy, curiosity, 0, List.of(traits));
    }

    /**
     * Returns this organism with updated energy clamped at zero.
     */
    public Organism withEnergy(int nextEnergy) {
        return new Organism(id, type, Math.max(0, nextEnergy), curiosity, generation, traits);
    }

    /**
     * Returns this organism with updated curiosity clamped at zero.
     */
    public Organism withCuriosity(int nextCuriosity) {
        return new Organism(id, type, energy, Math.max(0, nextCuriosity), generation, traits);
    }

    /**
     * Returns this organism with a different taxonomy type.
     */
    public Organism withType(OrganismType nextType) {
        return new Organism(id, nextType, energy, curiosity, generation, traits);
    }

    /**
     * Adds a trait when it is nonblank, new to the organism, and within the compact trait limit.
     */
    public Organism withTrait(String trait) {
        if (trait == null || trait.isBlank() || traits.contains(trait)) {
            return this;
        }
        return new Organism(id, type, energy, curiosity, generation, appendTrait(trait));
    }

    /**
     * Creates a child organism with inherited energy, increased curiosity, and one new trait.
     */
    public Organism child(String childId, OrganismType childType, String trait) {
        int childEnergy = Math.max(3, energy / 2);
        int childCuriosity = Math.max(1, curiosity + 1);
        return new Organism(childId, childType, childEnergy, childCuriosity, generation + 1, appendTrait(trait));
    }

    /**
     * Returns traits as compact comma-separated text for rendering.
     */
    public String traitText() {
        if (traits.isEmpty()) {
            return "quiet";
        }
        return traits.stream().collect(Collectors.joining(","));
    }

    /**
     * Builds the single-line organism description used by {@link GardenRenderer}.
     */
    public String describe() {
        return "%s %s energy=%d curiosity=%d generation=%d traits=%s".formatted(
                id,
                type.displayName(),
                energy,
                curiosity,
                generation,
                traitText()
        );
    }

    private List<String> appendTrait(String trait) {
        if (traits.size() >= 5) {
            return traits;
        }
        java.util.ArrayList<String> nextTraits = new java.util.ArrayList<>(traits);
        nextTraits.add(trait);
        return nextTraits;
    }
}
