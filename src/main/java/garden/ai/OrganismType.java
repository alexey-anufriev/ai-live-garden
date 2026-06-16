package garden.ai;

/**
 * A deliberately small taxonomy. Future agents may extend it, but should keep the vocabulary coherent.
 */
public enum OrganismType {

    MOSS("moss cluster"),
    ROOT_NETWORK("curious root network"),
    SPORE("dormant spore");

    private final String displayName;

    OrganismType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
