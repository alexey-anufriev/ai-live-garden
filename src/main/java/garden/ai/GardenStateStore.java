package garden.ai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the persistent living garden snapshot.
 *
 * <p>The format is deliberately line-oriented and dependency-free so future agents can inspect and evolve it.
 */
public final class GardenStateStore {

    public static final Path DEFAULT_PATH = Path.of("data", "garden-state.txt");

    private GardenStateStore() {
    }

    /**
     * Loads a garden snapshot from disk or returns {@link Garden#seed()} when the file does not exist.
     */
    public static Garden loadOrCreate(Path path) {
        if (!Files.exists(path)) {
            return Garden.seed();
        }
        try {
            return parse(Files.readAllLines(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read garden state from " + path, exception);
        }
    }

    /**
     * Writes a garden snapshot, creating parent directories when needed.
     */
    public static void save(Path path, Garden garden) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, format(garden), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write garden state to " + path, exception);
        }
    }

    /**
     * Serializes a garden snapshot into the committed line-oriented state format.
     */
    public static String format(Garden garden) {
        StringBuilder builder = new StringBuilder();
        builder.append("# AI Live Garden persistent state\n");
        builder.append("# This file is intentionally committed. The simulation reads it, advances it, and writes it back.\n");
        builder.append("version=2\n");
        builder.append("cycle=").append(garden.cycle()).append('\n');
        builder.append("nextId=").append(garden.nextId()).append('\n');
        builder.append("light=").append(garden.environment().light()).append('\n');
        builder.append("moisture=").append(garden.environment().moisture()).append('\n');
        builder.append("warmth=").append(garden.environment().warmth()).append('\n');
        builder.append("nutrients=").append(garden.environment().nutrients()).append('\n');
        builder.append("nutrientBuffer=").append(garden.environment().nutrientBuffer()).append('\n');
        builder.append('\n');

        for (Organism organism : garden.organisms()) {
            builder.append("organism=")
                    .append(escape(organism.id())).append('|')
                    .append(organism.type().name()).append('|')
                    .append(organism.energy()).append('|')
                    .append(organism.curiosity()).append('|')
                    .append(organism.generation()).append('|')
                    .append(escape(String.join(",", organism.traits())))
                    .append('\n');
        }
        builder.append('\n');

        for (GardenEvent event : garden.events()) {
            builder.append("event=")
                    .append(event.cycle()).append('|')
                    .append(escape(event.description()))
                    .append('\n');
        }
        return builder.toString();
    }

    private static Garden parse(List<String> lines) {
        int cycle = 0;
        int nextId = 1;
        int light = 50;
        int moisture = 60;
        int warmth = 42;
        int nutrients = 50;
        int nutrientBuffer = 50;
        List<Organism> organisms = new ArrayList<>();
        List<GardenEvent> events = new ArrayList<>();

        for (String line : lines) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("cycle=")) {
                cycle = parseIntValue(line, "cycle=");
            } else if (line.startsWith("nextId=")) {
                nextId = parseIntValue(line, "nextId=");
            } else if (line.startsWith("light=")) {
                light = parseIntValue(line, "light=");
            } else if (line.startsWith("moisture=")) {
                moisture = parseIntValue(line, "moisture=");
            } else if (line.startsWith("warmth=")) {
                warmth = parseIntValue(line, "warmth=");
            } else if (line.startsWith("nutrients=")) {
                nutrients = parseIntValue(line, "nutrients=");
            } else if (line.startsWith("nutrientBuffer=")) {
                nutrientBuffer = parseIntValue(line, "nutrientBuffer=");
            } else if (line.startsWith("organism=")) {
                organisms.add(parseOrganism(line.substring("organism=".length())));
            } else if (line.startsWith("event=")) {
                events.add(parseEvent(line.substring("event=".length())));
            }
        }

        if (organisms.isEmpty()) {
            return Garden.seed();
        }
        if (nextId < 1) {
            nextId = organisms.size() + 1;
        }
        return new Garden(cycle, nextId, new Environment(light, moisture, warmth, nutrients, nutrientBuffer), organisms, events);
    }

    private static Organism parseOrganism(String raw) {
        List<String> parts = splitEscaped(raw, '|');
        if (parts.size() < 6) {
            throw new IllegalArgumentException("Invalid organism state line: " + raw);
        }
        String id = unescape(parts.get(0));
        OrganismType type = OrganismType.valueOf(parts.get(1));
        int energy = Integer.parseInt(parts.get(2));
        int curiosity = Integer.parseInt(parts.get(3));
        int generation = Integer.parseInt(parts.get(4));
        String traitText = unescape(parts.get(5));
        List<String> traits = traitText.isBlank() ? List.of() : splitEscaped(traitText, ',').stream()
                .map(GardenStateStore::unescape)
                .filter(trait -> !trait.isBlank())
                .toList();
        return new Organism(id, type, energy, curiosity, generation, traits);
    }

    private static GardenEvent parseEvent(String raw) {
        List<String> parts = splitEscaped(raw, '|');
        if (parts.size() < 2) {
            throw new IllegalArgumentException("Invalid event state line: " + raw);
        }
        return new GardenEvent(Integer.parseInt(parts.get(0)), unescape(parts.get(1)));
    }

    private static int parseIntValue(String line, String prefix) {
        return Integer.parseInt(line.substring(prefix.length()).trim());
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    private static String unescape(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (escaping) {
                if (current == 'n') {
                    builder.append('\n');
                } else {
                    builder.append(current);
                }
                escaping = false;
            } else if (current == '\\') {
                escaping = true;
            } else {
                builder.append(current);
            }
        }
        if (escaping) {
            builder.append('\\');
        }
        return builder.toString();
    }

    private static List<String> splitEscaped(String value, char delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (escaping) {
                currentPart.append('\\').append(current);
                escaping = false;
            } else if (current == '\\') {
                escaping = true;
            } else if (current == delimiter) {
                result.add(currentPart.toString());
                currentPart.setLength(0);
            } else {
                currentPart.append(current);
            }
        }
        if (escaping) {
            currentPart.append('\\');
        }
        result.add(currentPart.toString());
        return result;
    }
}
