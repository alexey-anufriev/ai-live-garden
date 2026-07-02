package garden.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Calculates reproduction outcomes for organisms based on environment and traits.
 */
public class ReproductionCalculator {

    public record ReproductionContext(
            Environment environment,
            List<Organism> organisms,
            int cycle,
            int nextId,
            List<GardenEvent> events,
            int fungalContribution
    ) {}

    public record ReproductionResult(List<Organism> organisms, int nextId) {}

    public static ReproductionResult calculate(ReproductionContext context) {
        List<Organism> next = new ArrayList<>();
        int identifier = context.nextId();
        int birthsThisCycle = 0;
        long fungusCount = context.organisms().stream().filter(o -> o.type() == OrganismType.FUNGUS).count();

        for (Organism organism : context.organisms()) {
            OrganismType childType = organism.type().offspringType(context.cycle(), organism.generation(), context.environment());
            
            // Fungal role rescue mechanism
            if (fungusCount == 0 && organism.type() == OrganismType.ROOT_NETWORK) {
                childType = OrganismType.FUNGUS;
            }

            boolean isFungalSuccession = (organism.type() == OrganismType.ROOT_NETWORK && childType == OrganismType.FUNGUS);
            boolean canReproduce = (organism.energy() >= reproductionThreshold(organism, context.environment(), context.fungalContribution()) || (isFungalSuccession && organism.energy() >= 5)) && (birthsThisCycle < 2 || isFungalSuccession);

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
                next.add(parentAfterBirth);
                next.add(child);
                context.events().add(new GardenEvent(context.cycle(), "%s released %s as a new %s.".formatted(
                        organism.id(), child.id(), childType.displayName())));
                identifier++;
                birthsThisCycle++;
            } else {
                next.add(organism);
            }
        }

        next.sort(Comparator.comparing(Organism::id));
        return new ReproductionResult(next, identifier);
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
