package garden.ai;

import java.util.Comparator;
import java.util.Map;
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

        long moss = garden.organisms().stream().filter(o -> o.type() == OrganismType.MOSS).count();
        long roots = garden.organisms().stream().filter(o -> o.type() == OrganismType.ROOT_NETWORK).count();
        long spores = garden.organisms().stream().filter(o -> o.type() == OrganismType.SPORE).count();
        long ferns = garden.organisms().stream().filter(o -> o.type() == OrganismType.FERN).count();
        long beetles = garden.organisms().stream().filter(o -> o.type() == OrganismType.BEETLE).count();
        long hares = garden.organisms().stream().filter(o -> o.type() == OrganismType.HARE).count();
        long foxes = garden.organisms().stream().filter(o -> o.type() == OrganismType.FOX).count();

        long mossConserverCount = garden.organisms().stream().filter(o -> o.type() == OrganismType.MOSS && o.traits().contains("nutrient-conserver")).count();
        long mossScavengerCount = garden.organisms().stream().filter(o -> o.type() == OrganismType.MOSS && o.traits().contains("moss-nutrient-scavenger")).count();
        long fernConserverCount = garden.organisms().stream().filter(o -> o.type() == OrganismType.FERN && o.traits().contains("nutrient-conserver")).count();
        int consumptionReduction = (int) ((mossConserverCount + mossScavengerCount + fernConserverCount) / 10);
        int mobilizerCount = (int) garden.organisms().stream().filter(o -> o.traits().contains("nutrient-mobilizer")).count();

        Map<String, Long> traitCounts = garden.organisms().stream()
                .flatMap(o -> o.traits().stream())
                .collect(Collectors.groupingBy(java.util.function.Function.identity(), Collectors.counting()));

        String traitOverview = traitCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(", "));

        return """
                AI Live Garden
                ==============

                Cycle: %d
                Environment: light=%d moisture=%d warmth=%d nutrients=%d (buffer: %d, root: %d, fungal: %d) mood=%s
                Balance: plants=%d (Moss: %d, Roots: %d, Spores: %d, Ferns: %d) animals=%d (Beetles: %d, Hares: %d, Foxes: %d) total=%d
                Traits: %s

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
                garden.fungalContribution(),
                garden.environment().mood() + (garden.environment().mood().equals("hungry") ? " (" + garden.environment().diagnostic(moss, ferns, consumptionReduction, mobilizerCount) + ")" : ""),
                garden.plantCount(),
                moss, roots, spores, ferns,
                garden.animalCount(),
                beetles, hares, foxes,
                garden.organisms().size(),
                traitOverview,
                organisms,
                recentEvents
        );
    }
}
