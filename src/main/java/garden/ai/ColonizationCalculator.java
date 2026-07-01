package garden.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Calculates emergency reseeding and colonization outcomes for the garden.
 */
public class ColonizationCalculator {

    public record ColonizationContext(
            List<Organism> organisms,
            Environment environment,
            int cycle,
            int nextId,
            List<GardenEvent> events
    ) {}

    public record ColonizationResult(List<Organism> organisms, int nextId) {}

    public static ColonizationResult calculate(ColonizationContext context) {
        List<Organism> organisms = new ArrayList<>(context.organisms());
        int nextIdentifier = context.nextId();
        Random random = new Random();

        if (organisms.isEmpty()) {
            OrganismType[] plantTypes = {OrganismType.MOSS, OrganismType.SPORE, OrganismType.FERN, OrganismType.FUNGUS};
            OrganismType selected = plantTypes[random.nextInt(plantTypes.length)];
            String id = selected.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + nextIdentifier;
            organisms.add(Organism.of(id, selected, 5, 1, "emergency-seed"));
            context.events().add(new GardenEvent(context.cycle(), "A last emergency %s appeared to keep the garden alive.".formatted(selected.displayName())));
            nextIdentifier++;
        }

        long currentAnimalCount = organisms.stream().filter(o -> o.type().isAnimal()).count();
        long currentPlantCount = organisms.stream().filter(o -> o.type().isPlant()).count();
        if (currentAnimalCount == 0 && currentPlantCount > 200 && !Boolean.getBoolean("disable.emergency.colonization") && random.nextInt(20) == 0) {
            OrganismType herbivore = OrganismType.BEETLE;
            String id = herbivore.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + nextIdentifier;
            organisms.add(Organism.of(id, herbivore, 5, 2, "emergency-colonizer"));
            context.events().add(new GardenEvent(context.cycle(), "A new %s arrived to colonize the garden.".formatted(herbivore.displayName())));
            nextIdentifier++;
        }

        long herbivoreCount = organisms.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.HERBIVORE).count();
        long predatorCount = organisms.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.PREDATOR).count();
        if (herbivoreCount > 0 && predatorCount < 3 && !Boolean.getBoolean("disable.emergency.colonization") && random.nextInt(20) == 0) {
            OrganismType predator = OrganismType.FOX;
            String id = predator.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + nextIdentifier;
            organisms.add(Organism.of(id, predator, 5, 8, "emergency-colonizer"));
            context.events().add(new GardenEvent(context.cycle(), "A new %s arrived to colonize the garden.".formatted(predator.displayName())));
            nextIdentifier++;
        }

        return new ColonizationResult(organisms, nextIdentifier);
    }
}
