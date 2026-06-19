package garden.ai;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Converts garden snapshots into compact terminal-readable reports.
 */
public final class GardenRenderer {

    private GardenRenderer() {
    }

    /**
     * Renders the current environment, organism roster, and most recent events.
     *
     * @param garden snapshot to display
     * @return multiline text suitable for CLI output or logs
     */
    public static String render(Garden garden) {
        String organisms = garden.organisms().stream()
                .sorted(Comparator.comparing(Organism::id))
                .map(organism -> "- " + organism.describe())
                .collect(Collectors.joining(System.lineSeparator()));

        String recentEvents = garden.events().stream()
                .skip(Math.max(0, garden.events().size() - 8))
                .map(event -> "- cycle %d: %s".formatted(event.cycle(), event.description()))
                .collect(Collectors.joining(System.lineSeparator()));

        return """
                AI Live Garden
                ==============

                Cycle: %d
                Environment: light=%d moisture=%d warmth=%d nutrients=%d (buffer: %d, root: %d) mood=%s
                Balance: plants=%d animals=%d total=%d

                Organisms:
                %s

                Recent events:
                %s
                """.formatted(
                garden.cycle(),
                garden.environment().light(),
                garden.environment().moisture(),
                garden.environment().warmth(),
                garden.environment().nutrients(),
                garden.environment().nutrientBuffer(),
                garden.rootContribution(),
                garden.environment().mood() + (garden.environment().mood().equals("hungry") ? " (" + garden.environment().diagnostic() + ")" : ""),
                garden.plantCount(),
                garden.animalCount(),
                garden.organisms().size(),
                organisms,
                recentEvents
        );
    }
}
