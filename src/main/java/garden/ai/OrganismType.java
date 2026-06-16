package garden.ai;

import java.util.Set;

/**
 * A small taxonomy that can support both botanical growth and animal predation.
 * Future agents may extend it, but should keep the vocabulary coherent.
 */
public enum OrganismType {

    MOSS("moss cluster", Kingdom.PLANT, 1, Set.of()),
    ROOT_NETWORK("curious root network", Kingdom.PLANT, 2, Set.of()),
    SPORE("dormant spore", Kingdom.PLANT, 1, Set.of()),
    FERN("listening fern", Kingdom.PLANT, 2, Set.of()),
    BEETLE("amber beetle", Kingdom.HERBIVORE, 1, Set.of(MOSS, SPORE, FERN)),
    HARE("glass hare", Kingdom.HERBIVORE, 1, Set.of(MOSS, FERN, SPORE)),
    FOX("echo fox", Kingdom.PREDATOR, 2, Set.of(BEETLE, HARE));

    private final String displayName;
    private final Kingdom kingdom;
    private final int metabolism;
    private final Set<OrganismType> prey;

    OrganismType(String displayName, Kingdom kingdom, int metabolism, Set<OrganismType> prey) {
        this.displayName = displayName;
        this.kingdom = kingdom;
        this.metabolism = metabolism;
        this.prey = prey;
    }

    public String displayName() {
        return displayName;
    }

    public Kingdom kingdom() {
        return kingdom;
    }

    public int metabolism() {
        return metabolism;
    }

    public boolean isPlant() {
        return kingdom == Kingdom.PLANT;
    }

    public boolean isAnimal() {
        return kingdom == Kingdom.HERBIVORE || kingdom == Kingdom.PREDATOR;
    }

    public boolean canEat(OrganismType other) {
        return prey.contains(other);
    }

    public OrganismType offspringType(int cycle, int generation) {
        if (this == SPORE && (cycle + generation) % 3 == 0) {
            return MOSS;
        }
        if (this == MOSS && (cycle + generation) % 5 == 0) {
            return FERN;
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
