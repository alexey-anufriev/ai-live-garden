package garden.ai;

import java.util.Set;

/**
 * A small taxonomy that can support both botanical growth and animal predation.
 * Future agents may extend it, but should keep the vocabulary coherent.
 */
public enum OrganismType {

    MOSS("moss cluster", Kingdom.PLANT, 1, 2, Set.of()),
    ROOT_NETWORK("curious root network", Kingdom.PLANT, 2, 4, Set.of()),
    SPORE("dormant spore", Kingdom.PLANT, 1, 1, Set.of()),
    FERN("listening fern", Kingdom.PLANT, 2, 3, Set.of()),
    FUNGUS("mycelium network", Kingdom.PLANT, 1, 3, Set.of()),
    BEETLE("amber beetle", Kingdom.HERBIVORE, 1, 2, Set.of(MOSS, SPORE, FERN, ROOT_NETWORK, FUNGUS)),
    HARE("glass hare", Kingdom.HERBIVORE, 1, 5, Set.of(MOSS, FERN, SPORE, ROOT_NETWORK, FUNGUS)),
    FOX("echo fox", Kingdom.PREDATOR, 2, 8, Set.of(BEETLE, HARE));

    private final String displayName;
    private final Kingdom kingdom;
    private final int metabolism;
    private final int nutrientValue;
    private final Set<OrganismType> prey;

    OrganismType(String displayName, Kingdom kingdom, int metabolism, int nutrientValue, Set<OrganismType> prey) {
        this.displayName = displayName;
        this.kingdom = kingdom;
        this.metabolism = metabolism;
        this.nutrientValue = nutrientValue;
        this.prey = prey;
    }

    /**
     * Human-readable organism name used in events and rendering.
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Broad ecological role used to order and classify organisms.
     */
    public Kingdom kingdom() {
        return kingdom;
    }

    /**
     * Energy cost paid by animal organisms each cycle before feeding.
     */
    public int metabolism() {
        return metabolism;
    }

    /**
     * Nutrient value contributed to the soil upon death.
     */
    public int nutrientValue() {
        return nutrientValue;
    }

    /**
     * Returns whether this type grows from environmental conditions instead of spending metabolism.
     */
    public boolean isPlant() {
        return kingdom == Kingdom.PLANT;
    }

    /**
     * Returns whether this type participates in animal movement and feeding phases.
     */
    public boolean isAnimal() {
        return kingdom == Kingdom.HERBIVORE || kingdom == Kingdom.PREDATOR;
    }

    /**
     * Returns whether this type can feed on another organism type.
     */
    public boolean canEat(OrganismType other) {
        return prey.contains(other);
    }

    /**
     * Returns the set of organism types this type can feed on.
     */
    public Set<OrganismType> prey() {
        return prey;
    }

    /**
     * Chooses the child type released during reproduction.
     *
     * <p>Most organisms reproduce as their own type. A few deterministic transitions give the garden
     * simple succession behavior over time.
     */
    public OrganismType offspringType(int cycle, int generation) {
        if (this == SPORE && (cycle + generation) % 3 == 0) {
            return MOSS;
        }
        if (this == MOSS && (cycle + generation) % 5 == 0) {
            return FERN;
        }
        if (this == FERN && (cycle + generation) % 9 == 0) {
            return SPORE;
        }
        if (this == HARE && (cycle + generation) % 7 == 0) {
            return BEETLE;
        }
        return this;
    }

    public enum Kingdom {
        PLANT,
        HERBIVORE,
        PREDATOR
    }
}
