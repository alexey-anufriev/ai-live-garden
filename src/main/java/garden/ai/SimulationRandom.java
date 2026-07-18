package garden.ai;

import java.util.Random;
import java.util.function.Supplier;

/** Provides one reproducible random stream for every phase of a simulation run. */
final class SimulationRandom {

    private static final ThreadLocal<Random> CURRENT = ThreadLocal.withInitial(Random::new);

    private SimulationRandom() {
    }

    static Random current() {
        return CURRENT.get();
    }

    static <T> T with(Random random, Supplier<T> action) {
        Random previous = CURRENT.get();
        CURRENT.set(random);
        try {
            return action.get();
        } finally {
            CURRENT.set(previous);
        }
    }
}
