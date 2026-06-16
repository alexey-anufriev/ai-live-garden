package garden.ai;

/**
 * Small facade for advancing gardens by a fixed number of cycles.
 */
public final class Simulation {

    private Simulation() {
    }

    /**
     * Advances a fresh seed garden.
     */
    public static Garden run(int cycles) {
        return advance(Garden.seed(), cycles);
    }

    /**
     * Advances an existing garden snapshot.
     *
     * @param garden starting snapshot
     * @param cycles number of cycles to apply; must not be negative
     * @return the advanced snapshot
     */
    public static Garden advance(Garden garden, int cycles) {
        if (cycles < 0) {
            throw new IllegalArgumentException("cycles must not be negative");
        }
        Garden current = garden;
        for (int i = 0; i < cycles; i++) {
            current = current.nextCycle();
        }
        return current;
    }
}
