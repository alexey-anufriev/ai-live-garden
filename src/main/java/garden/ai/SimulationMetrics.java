package garden.ai;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/** Runs a bounded, read-only simulation and renders machine-readable ecological metrics. */
public final class SimulationMetrics {

    private SimulationMetrics() {
    }

    public static Report evaluate(Garden initial, int steps, long seed, int maxOrganisms) {
        Garden current = initial;
        EnumMap<OrganismType, Long> minimumCounts = counts(initial);
        EnumMap<OrganismType, Long> maximumCounts = counts(initial);
        int minimumTotal = initial.organisms().size();
        int maximumTotal = minimumTotal;
        int completedSteps = 0;
        String status = "completed";
        Random random = new Random(seed);

        for (int step = 0; step < steps; step++) {
            current = Simulation.advance(current, 1, random);
            completedSteps++;
            EnumMap<OrganismType, Long> currentCounts = counts(current);
            for (OrganismType type : OrganismType.values()) {
                minimumCounts.merge(type, currentCounts.get(type), Math::min);
                maximumCounts.merge(type, currentCounts.get(type), Math::max);
            }
            minimumTotal = Math.min(minimumTotal, current.organisms().size());
            maximumTotal = Math.max(maximumTotal, current.organisms().size());
            if (current.organisms().size() > maxOrganisms) {
                status = "population-limit-exceeded";
                break;
            }
        }

        return new Report(seed, steps, completedSteps, status, snapshot(initial), snapshot(current),
                minimumTotal, maximumTotal, minimumCounts, maximumCounts);
    }

    private static Snapshot snapshot(Garden garden) {
        return new Snapshot(garden.cycle(), garden.organisms().size(), garden.environment().nutrients(),
                garden.environment().nutrientBuffer(), counts(garden));
    }

    private static EnumMap<OrganismType, Long> counts(Garden garden) {
        EnumMap<OrganismType, Long> counts = new EnumMap<>(OrganismType.class);
        for (OrganismType type : OrganismType.values()) {
            counts.put(type, 0L);
        }
        for (Organism organism : garden.organisms()) {
            counts.merge(organism.type(), 1L, Long::sum);
        }
        return counts;
    }

    public record Snapshot(int cycle, int total, int nutrients, int nutrientBuffer,
                           Map<OrganismType, Long> counts) {
        public Snapshot {
            counts = Map.copyOf(counts);
        }
    }

    public record Report(long seed, int requestedSteps, int completedSteps, String status,
                         Snapshot initial, Snapshot finalState, int minimumTotal, int maximumTotal,
                         Map<OrganismType, Long> minimumCounts, Map<OrganismType, Long> maximumCounts) {
        public Report {
            minimumCounts = Map.copyOf(minimumCounts);
            maximumCounts = Map.copyOf(maximumCounts);
        }

        public String toJson() {
            return "{" +
                    "\"seed\":" + seed + ',' +
                    "\"requestedSteps\":" + requestedSteps + ',' +
                    "\"completedSteps\":" + completedSteps + ',' +
                    "\"status\":\"" + status + "\"," +
                    "\"initial\":" + snapshotJson(initial) + ',' +
                    "\"final\":" + snapshotJson(finalState) + ',' +
                    "\"minimumTotal\":" + minimumTotal + ',' +
                    "\"maximumTotal\":" + maximumTotal + ',' +
                    "\"minimumCounts\":" + countsJson(minimumCounts) + ',' +
                    "\"maximumCounts\":" + countsJson(maximumCounts) +
                    '}';
        }

        private static String snapshotJson(Snapshot snapshot) {
            return "{" +
                    "\"cycle\":" + snapshot.cycle() + ',' +
                    "\"total\":" + snapshot.total() + ',' +
                    "\"nutrients\":" + snapshot.nutrients() + ',' +
                    "\"nutrientBuffer\":" + snapshot.nutrientBuffer() + ',' +
                    "\"counts\":" + countsJson(snapshot.counts()) +
                    '}';
        }

        private static String countsJson(Map<OrganismType, Long> counts) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (OrganismType type : OrganismType.values()) {
                if (!first) {
                    json.append(',');
                }
                first = false;
                json.append('"').append(type.name()).append("\":").append(counts.getOrDefault(type, 0L));
            }
            return json.append('}').toString();
        }
    }
}
