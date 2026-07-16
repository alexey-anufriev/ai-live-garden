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
                .filter(line -> line.contains("BEETLE"))
                .map(line -> {
                    // Very simple parser based on GardenStateStore.java
                    String[] parts = line.split(" ");
                    String id = parts[0];
                    OrganismType type = OrganismType.valueOf(parts[1]);
                    int energy = Integer.parseInt(parts[2].split("=")[1]);
                    int curiosity = Integer.parseInt(parts[3].split("=")[1]);
                    int generation = Integer.parseInt(parts[4].split("=")[1]);
                    String traitText = parts[5].split("=")[1];
                    List<String> traits = traitText.equals("[]") ? List.of() : List.of(traitText.split(","));
                    return new Organism(id, type, energy, curiosity, generation, traits);
                })
                .collect(Collectors.toList());

        System.out.println("Found " + organisms.size() + " beetles.");
        for (Organism o : organisms) {
            System.out.println("Beetle " + o.id() + " has " + o.traits().size() + " traits: " + o.traits());
        }
    }
}
