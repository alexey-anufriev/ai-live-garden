package garden.ai;

public final class Simulation {

    private Simulation() {
    }

    public static Garden run(int cycles) {
        return advance(Garden.seed(), cycles);
    }

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
