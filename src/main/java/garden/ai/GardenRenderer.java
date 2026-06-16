package garden.ai;

import java.util.Comparator;
import java.util.stream.Collectors;

public final class GardenRenderer {

    private GardenRenderer() {
    }

    public static String render(Garden garden) {
        String organisms = garden.organisms().stream()
                .sorted(Comparator.comparing(Organism::id))
                .map(organism -> "- " + organism.describe())
                .collect(Collectors.joining(System.lineSeparator()));

        String recentEvents = garden.events().stream()
                .skip(Math.max(0, garden.events().size() - 5))
                .map(event -> "- cycle %d: %s".formatted(event.cycle(), event.description()))
                .collect(Collectors.joining(System.lineSeparator()));

        return """
                AI Live Garden
                ==============

                Cycle: %d
                Environment: light=%d moisture=%d warmth=%d mood=%s

                Organisms:
                %s

                Recent events:
                %s
                """.formatted(
                garden.cycle(),
                garden.environment().light(),
                garden.environment().moisture(),
                garden.environment().warmth(),
                garden.environment().mood(),
                organisms,
                recentEvents
        );
    }
}
