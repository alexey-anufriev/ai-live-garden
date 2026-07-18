package garden.ai;

import java.util.Random;

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
        return advance(garden, cycles, new Random());
    }

    /** Advances a garden reproducibly from a fixed seed. */
    public static Garden advance(Garden garden, int cycles, long seed) {
        return advance(garden, cycles, new Random(seed));
    }

    static Garden advance(Garden garden, int cycles, Random random) {
        if (cycles < 0) {
            throw new IllegalArgumentException("cycles must not be negative");
        }
        return SimulationRandom.with(random, () -> {
            Garden current = garden;
            for (int i = 0; i < cycles; i++) {
                current = OrganismInteractionCalculator.advance(current);
            }
            return current;
        });
    }
}
