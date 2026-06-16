package garden.ai;

public final class Main {

    private Main() {
    }

    static void main(String[] args) {
        int cycles = parseCycles(args);
        Garden garden = Simulation.run(cycles);
        IO.println(GardenRenderer.render(garden));
    }

    private static int parseCycles(String[] args) {
        if (args.length == 0) {
            return 3;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            return 3;
        }
    }
}
