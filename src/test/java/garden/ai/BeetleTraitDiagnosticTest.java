package garden.ai;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class BeetleTraitDiagnosticTest {
    @Test
    public void diagnoseBeetleTraits() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("data/garden-state.txt"));
        List<Organism> organisms = lines.stream()
                .filter(line -> line.startsWith("organism=") && line.contains("|BEETLE|"))
                .map(line -> {
                    String data = line.substring("organism=".length());
                    String[] parts = data.split("\\|");
                    String id = parts[0];
                    OrganismType type = OrganismType.valueOf(parts[1]);
                    int energy = Integer.parseInt(parts[2]);
                    int curiosity = Integer.parseInt(parts[3]);
                    int generation = Integer.parseInt(parts[4]);
                    String traitText = parts[5];
                    List<String> traits = traitText.isEmpty() ? List.of() : List.of(traitText.split(","));
                    return new Organism(id, type, energy, curiosity, generation, traits);
                })
                .collect(Collectors.toList());

        System.out.println("Found " + organisms.size() + " beetles.");
        for (Organism o : organisms) {
            System.out.println("Beetle " + o.id() + " has " + o.traits().size() + " traits: " + o.traits());
        }
    }
}
