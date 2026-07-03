package garden.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Calculates reproduction and colonization outcomes for the garden.
 */
public class PopulationDynamicsCalculator {

    public record PopulationContext(
            Environment environment,
            List<Organism> organisms,
            int cycle,
            int nextId,
            List<GardenEvent> events,
            int fungalContribution,
            Random random
    ) {}

    public record PopulationResult(List<Organism> organisms, int nextId) {}

    public static PopulationResult calculate(PopulationContext context) {
        List<Organism> next = new ArrayList<>(context.organisms());
        int identifier = context.nextId();
        int birthsThisCycle = 0;
        long fungusCount = next.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();

        // Reproduction phase
        List<Organism> afterReproduction = new ArrayList<>();
        for (Organism organism : context.organisms()) {
            OrganismType childType = organism.type().offspringType(context.cycle(), organism.generation(), context.environment());
            
            // Fungal role rescue mechanism
            if (fungusCount == 0 && organism.type() == OrganismType.ROOT_NETWORK) {
                childType = OrganismType.FUNGUS;
            }

            boolean isFungalSuccession = (organism.type() == OrganismType.ROOT_NETWORK && childType == OrganismType.FUNGUS);
            boolean canReproduce = (organism.energy() >= reproductionThreshold(organism, context.environment(), context.fungalContribution()) || (isFungalSuccession && organism.energy() >= 4)) && (birthsThisCycle < 2 || isFungalSuccession);

            if (organism.traits().contains("stressed") && !organism.traits().contains("fungal-symbiote") && !isFungalSuccession) {
                canReproduce = false;
            }
            if (organism.traits().contains("starving") && !organism.traits().contains("resourceful-breeder")) {
                canReproduce = false;
            }
            if (organism.traits().contains("cautious-breeder") && context.environment().nutrients() < 10) {
                canReproduce = false;
            }

            if (canReproduce) {
                String childId = childType.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + identifier;
                Organism parentAfterBirth = organism.withEnergy(organism.energy() / 2);
                Organism child = organism.child(childId, childType, TraitRegistry.getMutationTrait(context.cycle(), organism, childType));
                afterReproduction.add(parentAfterBirth);
                afterReproduction.add(child);
                context.events().add(new GardenEvent(context.cycle(), "%s released %s as a new %s.".formatted(
                        organism.id(), child.id(), childType.displayName())));
                identifier++;
                birthsThisCycle++;
            } else {
                afterReproduction.add(organism);
            }
        }
        afterReproduction.sort(Comparator.comparing(Organism::id));
        next = afterReproduction;

        // Colonization phase
        int bufferBonus = context.environment().nutrientBuffer() / 10;
        Random random = context.random() != null ? context.random() : new Random();

        if (next.isEmpty()) {
            OrganismType[] plantTypes = {OrganismType.MOSS, OrganismType.SPORE, OrganismType.FERN, OrganismType.FUNGUS};
            OrganismType selected = plantTypes[random.nextInt(plantTypes.length)];
            String id = selected.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + identifier;
            next.add(Organism.of(id, selected, 5, 1, "emergency-seed"));
            context.events().add(new GardenEvent(context.cycle(), "A last emergency %s appeared to keep the garden alive.".formatted(selected.displayName())));
            identifier++;
        }

        long currentAnimalCount = next.stream().filter(o -> o.type().isAnimal()).count();
        long currentPlantCount = next.stream().filter(o -> o.type().isPlant()).count();
        if (currentAnimalCount == 0 && currentPlantCount > 200 && !Boolean.getBoolean("disable.emergency.colonization") && random.nextInt(20 - bufferBonus) == 0) {
            OrganismType herbivore = OrganismType.BEETLE;
            String id = herbivore.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + identifier;
            next.add(Organism.of(id, herbivore, 5, 2, "emergency-colonizer"));
            context.events().add(new GardenEvent(context.cycle(), "A new %s arrived to colonize the garden.".formatted(herbivore.displayName())));
            identifier++;
        }

        long herbivoreCount = next.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.HERBIVORE).count();
        long predatorCount = next.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.PREDATOR).count();
        if (herbivoreCount > 0 && predatorCount < 3 && !Boolean.getBoolean("disable.emergency.colonization") && random.nextInt(20 - bufferBonus) == 0) {
            OrganismType predator = OrganismType.FOX;
            String id = predator.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + identifier;
            next.add(Organism.of(id, predator, 5, 8, "emergency-colonizer"));
            context.events().add(new GardenEvent(context.cycle(), "A new %s arrived to colonize the garden.".formatted(predator.displayName())));
            identifier++;
        }

        return new PopulationResult(next, identifier);
    }

    public static int reproductionThreshold(Organism organism, Environment environment, int fungalContribution) {
        int threshold = 15;
        if (organism.type().isPlant()) {
            threshold = 14;
        } else if (organism.type() == OrganismType.FOX) {
            threshold = 15;
        }
        for (String trait : organism.traits()) {
            threshold += TraitRegistry.getReproductionThresholdModifier(trait, environment, fungalContribution);
        }
        return threshold;
    }
}
