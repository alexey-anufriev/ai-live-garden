package garden.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Immutable-ish garden snapshot. Methods return new garden instances instead of mutating the current one.
 */
public record Garden(int cycle, Environment environment, List<Organism> organisms, List<GardenEvent> events) {

    public Garden(int cycle, Environment environment, List<Organism> organisms, List<GardenEvent> events) {
        this.cycle = cycle;
        this.environment = environment;
        this.organisms = List.copyOf(organisms);
        this.events = List.copyOf(events);
    }

    public static Garden seed() {
        List<Organism> organisms = List.of(
                new Organism("moss-1", OrganismType.MOSS, 7, 1),
                new Organism("root-1", OrganismType.ROOT_NETWORK, 9, 5),
                new Organism("spore-1", OrganismType.SPORE, 3, 8)
        );
        List<GardenEvent> events = List.of(new GardenEvent(0, "The garden wakes as a small coherent seed."));
        return new Garden(0, new Environment(48, 62, 41), organisms, events);
    }

    public Garden nextCycle() {
        int nextCycle = cycle + 1;
        Environment nextEnvironment = environment.next(nextCycle);
        List<Organism> nextOrganisms = new ArrayList<>();
        List<GardenEvent> nextEvents = new ArrayList<>(events);

        for (Organism organism : organisms) {
            Organism changed = evolve(organism, nextEnvironment, nextCycle);
            nextOrganisms.add(changed);
            describeChange(organism, changed, nextEnvironment, nextCycle).ifPresent(nextEvents::add);
        }

        nextEvents.add(new GardenEvent(nextCycle,
                "The garden becomes %s after cycle %d.".formatted(nextEnvironment.mood(), nextCycle)));

        return new Garden(nextCycle, nextEnvironment, nextOrganisms, nextEvents);
    }

    private Organism evolve(Organism organism, Environment environment, int cycle) {
        return switch (organism.type()) {
            case MOSS -> organism.withEnergy(organism.energy() + (environment.moisture() > 55 ? 2 : 0));
            case ROOT_NETWORK -> organism
                    .withEnergy(organism.energy() + (environment.warmth() > 40 ? 1 : 0))
                    .withCuriosity(organism.curiosity() + (cycle % 2 == 0 ? 1 : 0));
            case SPORE -> organism.withCuriosity(organism.curiosity() + (environment.light() < 50 ? 2 : 0));
        };
    }

    private Optional<GardenEvent> describeChange(Organism before, Organism after, Environment environment, int cycle) {
        if (after.energy() > before.energy()) {
            return Optional.of(new GardenEvent(cycle, "%s gathered energy from the %s garden.".formatted(after.id(), environment.mood())));
        }
        if (after.curiosity() > before.curiosity()) {
            return Optional.of(new GardenEvent(cycle, "%s became more curious under changing conditions.".formatted(after.id())));
        }
        return Optional.empty();
    }
}
