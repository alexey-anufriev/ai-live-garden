package garden.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/**
 * Deterministic emergency controls for restoring an operable persisted garden without erasing ecological roles.
 */
public final class GardenRecovery {

    public static final int DEFAULT_MAX_ORGANISMS = 12_000;

    private GardenRecovery() {
    }

    /**
     * Reduces a runaway population with a fair round-robin selection across organism types.
     * Minority roles are retained before remaining capacity is shared by abundant types.
     */
    public static Garden limitPopulation(Garden garden, int maxOrganisms) {
        if (maxOrganisms < 1) {
            throw new IllegalArgumentException("maxOrganisms must be positive");
        }
        if (garden.organisms().size() <= maxOrganisms) {
            return garden;
        }

        EnumMap<OrganismType, List<Organism>> byType = new EnumMap<>(OrganismType.class);
        for (OrganismType type : OrganismType.values()) {
            byType.put(type, new ArrayList<>());
        }
        for (Organism organism : garden.organisms()) {
            byType.get(organism.type()).add(organism);
        }
        byType.values().forEach(group -> group.sort(Comparator.comparing(Organism::id)));

        EnumMap<OrganismType, Integer> selectedByType = new EnumMap<>(OrganismType.class);
        List<Organism> selected = new ArrayList<>(maxOrganisms);
        boolean selectionPossible = true;
        while (selected.size() < maxOrganisms && selectionPossible) {
            selectionPossible = false;
            for (OrganismType type : OrganismType.values()) {
                List<Organism> group = byType.get(type);
                int index = selectedByType.getOrDefault(type, 0);
                if (index < group.size() && selected.size() < maxOrganisms) {
                    selected.add(group.get(index));
                    selectedByType.put(type, index + 1);
                    selectionPossible = true;
                }
            }
        }
        selected.sort(Comparator.comparing(Organism::id));

        int removed = garden.organisms().size() - selected.size();
        List<GardenEvent> events = new ArrayList<>(garden.events());
        events.add(new GardenEvent(garden.cycle(),
                "Emergency stewardship restored operability by relocating %d organisms while preserving every available ecological role."
                        .formatted(removed)));
        return new Garden(garden.cycle(), garden.nextId(), garden.environment(), selected, events);
    }
}
