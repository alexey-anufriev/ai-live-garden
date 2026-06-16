package garden.ai;

public final class Simulation {

    private Simulation() {
    }

    public static Garden run(int cycles) {
        if (cycles < 0) {
            throw new IllegalArgumentException("cycles must not be negative");
        }
        Garden garden = Garden.seed();
        for (int i = 0; i < cycles; i++) {
            garden = garden.nextCycle();
        }
        return garden;
    }
}
