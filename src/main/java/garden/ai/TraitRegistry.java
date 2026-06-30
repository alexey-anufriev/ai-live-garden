package garden.ai;

import java.util.Map;
import java.util.List;

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

    private static final String[] TRAITS = {"deeper-memory", "brighter-sense", "quiet-hunger", "rain-wise", "shadow-tuned", "resilient", "sun-lover", "sun-seeker", "rain-collector", "nutrient-finder", "nutrient-efficient", "shadow-stepper", "hardy", "water-seeker", "dormancy", "nutrient-weaver", "metabolic-efficiency", "scavenger", "nutrient-sharer", "buffer-resonator", "buffer-scavenger", "nutrient-hoarder", "nutrient-scout", "soil-master", "deep-rooting", "buffer-optimizer", "buffer-tapper", "nutrient-translocator", "camouflaged", "shade-thriver", "moisture-retainer", "nutrient-absorber", "nutrient-synthesizer", "prey-tracker", "resource-tracker", "predator-focus", "nutrient-reclaimer", "nutrient-producer", "nutrient-enricher", "moisture-thriver", "prolific", "cautious-feeder", "nutrient-decomposer", "fungus-soil-enricher", "fungal-network-connector", "fungal-feeder", "mycorrhizal-booster", "nutrient-scrounger", "fungal-symbiote", "nutrient-pump", "nutrient-distributor", "resourceful-breeder", "fungal-enhancer", "mycelial-scavenger", "mycelial-harvester", "mycelial-distributor", "mycelial-resonator", "mycelial-network-scout", "fungal-gardener", "fungal-fertilizer", "nutrient-anticipator", "mycelial-protector", "metabolic-economizer", "spore-disperser", "fungal-root-symbiont", "mycelial-root-mediator", "fungal-attractor", "mycelial-conduit", "mycelial-synergizer", "moss-nutrient-scavenger", "cautious-breeder", "stress-resilient", "stress-avoidance", "buffer-siphon", "fungal-decomposer-mimic", "nutrient-harvester"};

    public record MetabolicEffect(int metabolismChange, int energyBonus, GardenEvent event) {}

    public record PlantGrowthEffect(int growthChange, GardenEvent event) {}

    public static int getNutrientValueModifier(String trait) {
        return NUTRIENT_VALUES.getOrDefault(trait, 0);
    }

    public static int getReproductionThresholdModifier(String trait, Environment environment, int fungalContribution) {
        int modifier = 0;
        switch (trait) {
            case "prolific":
                modifier -= 3;
                break;
            case "resourceful-breeder":
                if (environment.nutrients() < 20) modifier -= 3;
                break;
            case "fungal-nurturer":
                if (fungalContribution > 0) modifier -= 3;
                break;
        }
        return modifier;
    }

    public static String getMutationTrait(int cycle, Organism organism) {
        int index = Math.floorMod(organism.id().hashCode() + cycle + organism.generation(), TRAITS.length);
        return TRAITS[index];
    }

    public static PlantGrowthEffect getPlantGrowthEffect(String trait, int cycle, Organism organism, Environment environment, List<Organism> organisms, int fungalContribution) {
        switch (trait) {
            case "water-seeker":
                if (environment.moisture() < 50) return new PlantGrowthEffect(1, null);
                break;
            case "sun-seeker":
                if (environment.light() > 60) return new PlantGrowthEffect(1, new GardenEvent(cycle, "%s thrived in the sunlight.".formatted(organism.id())));
                break;
            case "sun-lover":
                if (environment.light() > 60) return new PlantGrowthEffect(1, null);
                break;
            case "shade-thriver":
                if (environment.light() < 40) return new PlantGrowthEffect(2, new GardenEvent(cycle, "%s thrived in the shade.".formatted(organism.id())));
                break;
            case "rain-collector":
                if (environment.moisture() < 40) return new PlantGrowthEffect(1, null);
                break;
            case "hardy":
                if (organism.type() == OrganismType.FERN && environment.warmth() > 50) return new PlantGrowthEffect(1, null);
                break;
            case "nutrient-efficient":
                if (environment.nutrients() < 30) return new PlantGrowthEffect(1, null);
                break;
            case "nutrient-synthesizer":
                if (environment.nutrients() < 5) return new PlantGrowthEffect(2, new GardenEvent(cycle, "%s synthesized nutrients from the soil.".formatted(organism.id())));
                break;
            case "buffer-tapper":
                if (environment.nutrients() < 5 && environment.nutrientBuffer() > 0) return new PlantGrowthEffect(2, new GardenEvent(cycle, "%s tapped the nutrient buffer.".formatted(organism.id())));
                break;
            case "buffer-resonator":
                if (environment.nutrientBuffer() > 0) return new PlantGrowthEffect(1, new GardenEvent(cycle, "%s utilized the nutrient buffer.".formatted(organism.id())));
                break;
            case "moisture-thriver":
                if (environment.moisture() > 60) return new PlantGrowthEffect(2, new GardenEvent(cycle, "%s thrived in the moisture.".formatted(organism.id())));
                break;
            case "fungal-feeder":
                if (fungalContribution > 0) {
                    boolean hasMycorrhizalBooster = organisms.stream().anyMatch(o -> o.type() == OrganismType.FUNGUS && o.traits().contains("mycorrhizal-booster"));
                    int bonus = hasMycorrhizalBooster ? 2 : 1;
                    return new PlantGrowthEffect(bonus, new GardenEvent(cycle, "%s fed on fungal networks%s.".formatted(organism.id(), hasMycorrhizalBooster ? " (boosted)" : "")));
                }
                break;
            case "resilient":
                return new PlantGrowthEffect(-1, null);
        }
        return null;
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

    public static long count(List<Organism> organisms, String trait) {
        return organisms.stream().filter(o -> o.traits().contains(trait)).count();
    }

    public static long count(List<Organism> organisms, String trait, OrganismType type) {
        return organisms.stream().filter(o -> o.type() == type && o.traits().contains(trait)).count();
    }

    public static long countAnimalTrait(List<Organism> organisms, String trait) {
        return organisms.stream().filter(o -> o.type().isAnimal() && o.traits().contains(trait)).count();
    }

    public static long countPlantTrait(List<Organism> organisms, String trait) {
        return organisms.stream().filter(o -> o.type().isPlant() && o.traits().contains(trait)).count();
    }
}

