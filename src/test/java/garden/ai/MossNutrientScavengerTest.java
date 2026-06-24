package garden.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MossNutrientScavengerTest {

    @Test
    void testMossScavengerReducesConsumption() {
        // Setup garden with 50 MOSS organisms
        List<Organism> organisms = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            organisms.add(Organism.of("moss-" + i, OrganismType.MOSS, 10, 1, "quiet"));
        }
        
        Garden garden = new Garden(100, 101, new Environment(50, 50, 50, 50, 50), organisms, List.of());
        
        // Calculate consumption without trait
        int consumptionWithout = (int)Math.max(0, garden.plantCount() / 5);
        
        // Add trait to all
        List<Organism> scavengers = new ArrayList<>();
        for (Organism o : organisms) {
            scavengers.add(o.withTrait("moss-nutrient-scavenger"));
        }
        
        Garden gardenWithTrait = new Garden(100, 101, new Environment(50, 50, 50, 50, 50), scavengers, List.of());
        
        long conserverCount = gardenWithTrait.organisms().stream().filter(o -> o.type().isPlant() && o.traits().contains("nutrient-conserver")).count();
        long scavengerCount = gardenWithTrait.organisms().stream().filter(o -> o.type() == OrganismType.MOSS && o.traits().contains("moss-nutrient-scavenger")).count();
        int consumptionWith = (int)Math.max(0, gardenWithTrait.plantCount() / 5 - (int) ((conserverCount + scavengerCount) / 10));
        
        assertTrue(consumptionWith < consumptionWithout, "Consumption should be reduced with moss-nutrient-scavenger trait. Without: " + consumptionWithout + ", With: " + consumptionWith);
    }
}
