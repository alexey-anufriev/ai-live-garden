package garden.ai;

import java.util.Map;

public class TraitRegistry {
    private static final Map<String, Integer> NUTRIENT_VALUES = Map.of(
            "nutrient-hoarder", 5,
            "nutrient-recycler", 3,
            "nutrient-decomposer", 7,
            "nutrient-enricher", 5,
            "nutrient-distributor", 4,
            "nutrient-sharer", 3,
            "nutrient-storer", 6
    );

    public record MetabolicEffect(int metabolismChange, int energyBonus, GardenEvent event) {}

    public static int getNutrientValueModifier(String trait) {
        return NUTRIENT_VALUES.getOrDefault(trait, 0);
    }

    public static MetabolicEffect getMetabolicEffect(String trait, int cycle, Organism organism, Environment environment, int fungalContribution, int mossContribution) {
        switch (trait) {
            case "resilient":
                return new MetabolicEffect(1, 0, null);
            case "dormancy":
                if (environment.nutrients() < 15) return new MetabolicEffect(-2, 0, null);
                break;
            case "metabolic-efficiency":
                return new MetabolicEffect(-1, 0, null);
            case "quiet-hunger":
                if (organism.traits().contains("starving")) return new MetabolicEffect(-1, 0, null);
                break;
            case "nutrient-sharer":
                if (organism.traits().contains("starving")) return new MetabolicEffect(-2, 0, new GardenEvent(cycle, "%s shared metabolic burden while starving.".formatted(organism.id())));
                break;
            case "moss-harvester":
                if (mossContribution > 0) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s harvested nutrients from mosses.".formatted(organism.id())));
                break;
            case "spore-disperser":
                if (fungalContribution > 0) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s dispersed spores near the fungal network.".formatted(organism.id())));
                break;
            case "mycelial-conduit":
                if (fungalContribution > 0) return new MetabolicEffect(0, 1, new GardenEvent(cycle, "%s channeled energy through the mycelial network.".formatted(organism.id())));
                break;
            case "mycelial-scavenger":
                if (fungalContribution > 0) {
                    int reduction = 2;
                    if (organism.traits().contains("mycelial-harvester")) {
                        reduction += 1;
                    }
                    return new MetabolicEffect(-reduction, 0, new GardenEvent(cycle, "%s scavenged nutrients from the mycelial network%s.".formatted(organism.id(), organism.traits().contains("mycelial-harvester") ? " (harvested)" : "")));
                }
                break;
            case "mycelial-resonator":
                if (fungalContribution > 0 && !organism.traits().contains("mycelial-scavenger")) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s resonated with the mycelial network.".formatted(organism.id())));
                break;
            case "mycelial-protector":
                if (fungalContribution > 0) return new MetabolicEffect(-2, 0, new GardenEvent(cycle, "%s was protected by the mycelial network.".formatted(organism.id())));
                break;
            case "nutrient-anticipator":
                if (environment.nutrientBuffer() < 20) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s anticipated nutrient scarcity.".formatted(organism.id())));
                break;
            case "metabolic-economizer":
                if (organism.traits().contains("stressed")) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s economically managed its metabolism while stressed.".formatted(organism.id())));
                break;
            case "buffer-scavenger":
                if (environment.nutrientBuffer() > 0) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s utilized the nutrient buffer.".formatted(organism.id())));
                break;
            case "buffer-explorer":
                if (environment.nutrientBuffer() > 0) {
                    if (organism.traits().contains("buffer-tapper") && environment.nutrients() < 10) return new MetabolicEffect(-1, 1, new GardenEvent(cycle, "%s explored and tapped additional buffer nutrients.".formatted(organism.id())));
                    else return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s explored the nutrient buffer.".formatted(organism.id())));
                }
                break;
            case "mycelial-buffer-adapter":
                if (fungalContribution > 0 && environment.nutrientBuffer() > 0) return new MetabolicEffect(-1, 1, new GardenEvent(cycle, "%s adapted to buffer tapping through the mycelial network.".formatted(organism.id())));
                break;
            case "buffer-tapper":
                if (environment.nutrients() < 10 && environment.nutrientBuffer() > 0) return new MetabolicEffect(0, 1, new GardenEvent(cycle, "%s tapped the nutrient buffer while starving.".formatted(organism.id())));
                break;
            case "nutrient-scrounger":
                if (environment.nutrients() < 25) return new MetabolicEffect(0, 1, new GardenEvent(cycle, "%s scrounged for nutrients.".formatted(organism.id())));
                break;
        }
        return null;
    }
}
