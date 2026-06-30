package garden.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculator for animal feeding bite size.
 */
public class FeedingBiteCalculator {

    public record FeedingBiteContext(
            Organism hunter,
            Organism prey,
            Environment environment,
            int cycle,
            long rootNetworkCount
    ) {}

    public record FeedingBiteResult(int biteSize, List<GardenEvent> events) {}

    public static FeedingBiteResult calculate(FeedingBiteContext context) {
        Organism hunter = context.hunter();
        Organism prey = context.prey();
        Environment environment = context.environment();
        int cycle = context.cycle();
        long rootNetworkCount = context.rootNetworkCount();
        List<GardenEvent> events = new ArrayList<>();

        int bite = hunter.type() == OrganismType.FOX ? 3 : 2;
        if (hunter.traits().contains("nutrient-finder")) {
            bite += 1;
        }
        if (hunter.traits().contains("scavenger") && environment.nutrients() < 25) {
            bite += 1;
        }
        if (hunter.traits().contains("nutrient-hoarder")) {
            bite += 1;
        }
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("predator-focus")) {
            bite += 1;
        }
        if (hunter.traits().contains("root-tapper") && rootNetworkCount > 0) {
            bite += 1;
        }
        if (hunter.traits().contains("nutrient-refiner")) {
            if (!hunter.traits().contains("stressed") || hunter.traits().contains("starving")) {
                bite += 1;
                if (hunter.traits().contains("starving")) {
                    events.add(new GardenEvent(cycle, "%s refined nutrients while starving.".formatted(hunter.id())));
                }
            }
        }
        if (hunter.traits().contains("gentle-feeder")) {
            bite = Math.max(1, bite - 1);
        }
        if (hunter.traits().contains("nutrient-reclaimer") && prey.traits().contains("nutrient-storer")) {
            bite += 2;
            events.add(new GardenEvent(cycle, "%s reclaimed extra nutrients from %s.".formatted(hunter.id(), prey.id())));
        }
        if (hunter.traits().contains("nutrient-harvester")) {
            bite += 1;
            events.add(new GardenEvent(cycle, "%s harvested additional nutrients.".formatted(hunter.id())));
        }
        return new FeedingBiteResult(bite, events);
    }
}
