package garden.ai;

import java.nio.file.Path;

/**
 * Command-line entry point for inspecting or advancing the persistent garden state.
 *
 * <p>Supported commands:
 * <pre>{@code
 * inspect
 * tick
 * tick --steps 3
 * tick 5
 * tick --state data/garden-state.txt
 * tick --steps 10 --state data/garden-state.txt
 * recover --max-organisms 12000
 * }</pre>
 *
 * <p>When no arguments are provided, the command defaults to {@code tick --steps 3}.
 * The {@code inspect} command reads and renders the current garden without changing it.
 * The {@code tick} command loads the current state, advances the simulation, and saves
 * the updated state back to disk.
 *
 * @param args command-line arguments controlling the command, step count, and state file
 */
public final class Main {

    private Main() {
    }

    static void main(String[] args) {
        Command command = Command.parse(args);
        Garden garden = GardenStateStore.loadOrCreate(command.statePath());

        if (command.mode() == Mode.INSPECT) {
            System.out.println(GardenRenderer.render(garden));
            return;
        }

        if (command.mode() == Mode.RECOVER) {
            Garden recovered = GardenRecovery.limitPopulation(garden, command.maxOrganisms());
            GardenStateStore.save(command.statePath(), recovered);
            System.out.println(GardenRenderer.render(recovered));
            System.out.println("Recovered persistent garden state at " + command.statePath());
            return;
        }

        Garden advanced = Simulation.advance(garden, command.steps());
        GardenStateStore.save(command.statePath(), advanced);
        System.out.println(GardenRenderer.render(advanced));
        System.out.println("Saved persistent garden state to " + command.statePath());
    }

    private record Command(Mode mode, int steps, Path statePath, int maxOrganisms) {

        static Command parse(String[] args) {
            Mode mode = Mode.TICK;
            int steps = 3;
            Path statePath = GardenStateStore.DEFAULT_PATH;
            int maxOrganisms = GardenRecovery.DEFAULT_MAX_ORGANISMS;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("tick".equalsIgnoreCase(arg)) {
                    mode = Mode.TICK;
                } else if ("inspect".equalsIgnoreCase(arg)) {
                    mode = Mode.INSPECT;
                } else if ("recover".equalsIgnoreCase(arg)) {
                    mode = Mode.RECOVER;
                } else if ("--steps".equals(arg) && i + 1 < args.length) {
                    steps = parsePositiveInt(args[++i], 3);
                } else if (arg.startsWith("--steps=")) {
                    steps = parsePositiveInt(arg.substring("--steps=".length()), 3);
                } else if ("--state".equals(arg) && i + 1 < args.length) {
                    statePath = Path.of(args[++i]);
                } else if (arg.startsWith("--state=")) {
                    statePath = Path.of(arg.substring("--state=".length()));
                } else if ("--max-organisms".equals(arg) && i + 1 < args.length) {
                    maxOrganisms = parsePositiveInt(args[++i], GardenRecovery.DEFAULT_MAX_ORGANISMS);
                } else if (arg.startsWith("--max-organisms=")) {
                    maxOrganisms = parsePositiveInt(arg.substring("--max-organisms=".length()),
                            GardenRecovery.DEFAULT_MAX_ORGANISMS);
                } else {
                    steps = parsePositiveInt(arg, steps);
                }
            }
            return new Command(mode, steps, statePath, maxOrganisms);
        }

        private static int parsePositiveInt(String value, int fallback) {
            try {
                int parsed = Integer.parseInt(value);
                return Math.max(0, parsed);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
    }

    private enum Mode {
        TICK,
        INSPECT,
        RECOVER
    }
}
