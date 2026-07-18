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

        long mossConserverCount = TraitRegistry.count(garden.organisms(), "nutrient-conserver", OrganismType.MOSS);
        long mossScavengerCount = TraitRegistry.count(garden.organisms(), "moss-nutrient-scavenger", OrganismType.MOSS);
        long fernConserverCount = TraitRegistry.count(garden.organisms(), "nutrient-conserver", OrganismType.FERN);
        long demandRegulatorCount = TraitRegistry.count(garden.organisms(), "nutrient-demand-regulator");
        int mossConsumptionReduction = (int) ((mossConserverCount + mossScavengerCount) / 5);
        int fernConsumptionReduction = (int) (fernConserverCount / 5);
        int rootConsumptionReduction = (int) (demandRegulatorCount / 2);
        int mobilizerCount = (int) TraitRegistry.count(garden.organisms(), "nutrient-mobilizer");
        int releaserCount = (int) TraitRegistry.count(garden.organisms(), "buffer-releaser");
        int acceleratorCount = (int) TraitRegistry.count(garden.organisms(), "buffer-release-accelerator");

        Map<String, Long> traitCounts = garden.organisms().stream()
                .flatMap(o -> o.traits().stream())
                .collect(Collectors.groupingBy(java.util.function.Function.identity(), Collectors.counting()));

        String traitOverview = traitCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(", "));

        long culledPlantCount = garden.events().stream()
            .filter(e -> e.cycle() == garden.cycle())
            .filter(e -> e.description().contains("was culled due to chronic environmental stress."))
            .count();
        long stressResilientPlantCount = garden.organisms().stream()
            .filter(o -> o.traits().contains("stress-resilient"))
            .count();

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
                garden.environment().mood() + (garden.environment().mood().equals("hungry") ? " (" + garden.environment().diagnostic(moss, ferns, mossConsumptionReduction, fernConsumptionReduction, rootConsumptionReduction, mobilizerCount, releaserCount, acceleratorCount, garden.blockedPlantCount(), culledPlantCount, stressResilientPlantCount) + ")" : ""),
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
