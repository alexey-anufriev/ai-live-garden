package garden.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Immutable garden snapshot and rule engine for one digital ecosystem.
 *
 * <p>Instances are treated as values: cycle advancement returns a new {@code Garden} with copied organism
 * and event lists. The simulation rules live here so persistence, rendering, and command-line handling can
 * stay thin.
 *
 * @param cycle completed cycle count
 * @param nextId next numeric suffix reserved for a newly born organism
 * @param environment current environmental conditions
 * @param organisms living organisms sorted by stable identifier after each cycle
 * @param events recent observable cycle events, capped to keep persistent state compact
 */
public record Garden(int cycle, int nextId, Environment environment, List<Organism> organisms, List<GardenEvent> events) {

    private static final int MAX_EVENTS = 40;

    public Garden(int cycle, int nextId, Environment environment, List<Organism> organisms, List<GardenEvent> events) {
        if (cycle < 0) {
            throw new IllegalArgumentException("cycle must not be negative");
        }
        if (nextId < 1) {
            throw new IllegalArgumentException("nextId must be positive");
        }
        this.cycle = cycle;
        this.nextId = nextId;
        this.environment = environment;
        this.organisms = List.copyOf(organisms);
        this.events = trimEvents(events);
    }

    /**
     * Creates the deterministic initial garden used when no persistent state exists.
     */
    public static Garden seed() {
        List<Organism> organisms = List.of(
                Organism.of("moss-1", OrganismType.MOSS, 9, 1, "shade-loving"),
                Organism.of("root-1", OrganismType.ROOT_NETWORK, 11, 5, "listens-below"),
                Organism.of("spore-1", OrganismType.SPORE, 4, 7, "patient"),
                Organism.of("fern-1", OrganismType.FERN, 9, 3, "wide-frond"),
                Organism.of("beetle-1", OrganismType.BEETLE, 9, 4, "amber-shell"),
                Organism.of("hare-1", OrganismType.HARE, 11, 6, "glass-footed"),
                Organism.of("fox-1", OrganismType.FOX, 10, 8, "echo-hunter")
        );
        List<GardenEvent> events = List.of(new GardenEvent(0,
                "The garden wakes with plants, grazers, and a watchful predator."));
        return new Garden(0, 8, new Environment(50, 64, 43, 58), organisms, events);
    }

    /**
     * Advances the garden by one cycle.
     *
     * <p>The phase order is environmental drift, passive organism changes, feeding, reproduction, emergency
     * reseeding if every organism dies, then a final cycle summary event.
     *
     * @return a new garden snapshot after one completed cycle
     */
    public Garden nextCycle() {
        int nextCycle = cycle + 1;
        long plantCount = organisms.stream().filter(organism -> organism.type().isPlant()).count();
        long animalCount = organisms.stream().filter(organism -> organism.type().isAnimal()).count();
        Environment nextEnvironment = environment.next(nextCycle, (int) plantCount, (int) animalCount);
        List<GardenEvent> nextEvents = new ArrayList<>(events);
        List<Organism> changed = organisms.stream()
                .map(organism -> passiveChange(organism, nextEnvironment, nextCycle, nextEvents))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        FeedingResult feeding = feedingPhase(changed, nextCycle, nextEvents);
        
        // Add nutrients based on deaths
        Environment environmentWithNutrients = nextEnvironment.withNutrients(feeding.deaths());
        
        ReproductionResult reproduction = reproductionPhase(feeding.organisms(), nextCycle, nextId, nextEvents);
        List<Organism> finalChanged = reproduction.organisms();
        int nextIdentifier = reproduction.nextId();
        
        if (finalChanged.isEmpty()) {
            finalChanged.add(Organism.of("moss-" + nextIdentifier, OrganismType.MOSS, 5, 1, "emergency-seed"));
            nextEvents.add(new GardenEvent(nextCycle, "A last emergency moss seed appeared to keep the garden alive."));
            nextIdentifier++;
        }
        
        nextEvents.add(new GardenEvent(nextCycle,
                "The garden becomes %s after cycle %d.".formatted(environmentWithNutrients.mood(), nextCycle)));
        
        return new Garden(nextCycle, nextIdentifier, environmentWithNutrients, finalChanged, nextEvents);
    }

    /**
     * Counts organisms in the plant kingdom.
     */
    public long plantCount() {
        return organisms.stream().filter(organism -> organism.type().isPlant()).count();
    }

    /**
     * Counts herbivores and predators.
     */
    public long animalCount() {
        return organisms.stream().filter(organism -> organism.type().isAnimal()).count();
    }

    private Organism passiveChange(Organism organism, Environment environment, int cycle, List<GardenEvent> events) {
        Organism changed = organism;
        if (organism.type().isPlant()) {
            int growth = environment.favorsPlants() ? 2 : 0;
            if (organism.type() == OrganismType.ROOT_NETWORK && environment.nutrients() > 45) {
                growth += 1;
            }
            if (organism.type() == OrganismType.MOSS && environment.moisture() > 60) {
                growth += 1;
            }
            if (organism.type() == OrganismType.SPORE && environment.light() < 45) {
                changed = changed.withCuriosity(changed.curiosity() + 2);
            }
            changed = changed.withEnergy(changed.energy() + growth);
        } else {
            changed = changed.withEnergy(changed.energy() - organism.type().metabolism())
                    .withCuriosity(changed.curiosity() + (cycle % 4 == 0 ? 1 : 0));
        }

        maybeDescribeChange(organism, changed, environment, cycle).ifPresent(events::add);

        if (organism.type().isPlant() && !environment.favorsPlants() && !changed.traits().contains("resilient")) {
            changed = changed.withTrait("stressed");
        } else if (organism.type().isAnimal() && environment.nutrients() < 25 && !changed.traits().contains("resilient")) {
            changed = changed.withTrait("starving");
        }

        return maybeMutate(changed, cycle, events);
    }

    private FeedingResult feedingPhase(List<Organism> organisms, int cycle, List<GardenEvent> events) {
        List<Organism> mutable = new ArrayList<>(organisms);
        mutable.sort(Comparator.comparing((Organism organism) -> organism.type().kingdom().ordinal())
                .thenComparing(Organism::id));

        for (int hunterIndex = 0; hunterIndex < mutable.size(); hunterIndex++) {
            Organism hunter = mutable.get(hunterIndex);
            if (!hunter.type().isAnimal() || hunter.energy() <= 0) {
                continue;
            }
            Optional<Integer> preyIndex = findPreyIndex(mutable, hunter, hunterIndex);
            if (preyIndex.isEmpty()) {
                continue;
            }

            int index = preyIndex.get();
            Organism prey = mutable.get(index);
            int bite = hunter.type() == OrganismType.FOX ? 3 : 2;
            Organism fedHunter = hunter.withEnergy(hunter.energy() + bite).withTrait("fed-" + cycle);
            Organism weakenedPrey = prey.withEnergy(prey.energy() - bite);
            mutable.set(hunterIndex, fedHunter);
            mutable.set(index, weakenedPrey);
            events.add(new GardenEvent(cycle, "%s fed on %s.".formatted(hunter.id(), prey.id())));
        }

        int deaths = 0;
        List<Organism> survivors = new ArrayList<>();
        for (Organism organism : mutable) {
            if (organism.energy() > 0) {
                survivors.add(organism);
            } else {
                events.add(new GardenEvent(cycle, "%s returned to the soil.".formatted(organism.id())));
                deaths++;
            }
        }
        survivors.sort(Comparator.comparing(Organism::id));
        return new FeedingResult(survivors, deaths);
    }

    private record FeedingResult(List<Organism> organisms, int deaths) {
    }

    private Optional<Integer> findPreyIndex(List<Organism> organisms, Organism hunter, int hunterIndex) {
        for (int i = 0; i < organisms.size(); i++) {
            if (i == hunterIndex) {
                continue;
            }
            Organism candidate = organisms.get(i);
            if (candidate.energy() > 0 && hunter.type().canEat(candidate.type())) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private ReproductionResult reproductionPhase(List<Organism> organisms, int cycle, int nextId, List<GardenEvent> events) {
        List<Organism> next = new ArrayList<>();
        int identifier = nextId;
        int birthsThisCycle = 0;

        for (Organism organism : organisms) {
            boolean canReproduce = organism.energy() >= reproductionThreshold(organism) && birthsThisCycle < 2;

            if (organism.traits().contains("stressed") || organism.traits().contains("starving")) {
                canReproduce = false;
            }

            if (canReproduce) {
                OrganismType childType = organism.type().offspringType(cycle, organism.generation());
                String childId = childType.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-') + "-" + identifier;
                Organism parentAfterBirth = organism.withEnergy(organism.energy() / 2);
                Organism child = organism.child(childId, childType, mutationTrait(cycle, organism));
                next.add(parentAfterBirth);
                next.add(child);
                events.add(new GardenEvent(cycle, "%s released %s as a new %s.".formatted(
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

    private int reproductionThreshold(Organism organism) {
        if (organism.type().isPlant()) {
            return 14;
        }
        if (organism.type() == OrganismType.FOX) {
            return 18;
        }
        return 15;
    }

    private Organism maybeMutate(Organism organism, int cycle, List<GardenEvent> events) {
        if ((organism.energy() + organism.curiosity() + cycle + organism.generation()) % 11 != 0) {
            return organism;
        }
        String trait = mutationTrait(cycle, organism);
        Organism changed = organism.withTrait(trait).withCuriosity(organism.curiosity() + 1);
        events.add(new GardenEvent(cycle, "%s adapted a %s trait.".formatted(organism.id(), trait)));
        return changed;
    }

    private String mutationTrait(int cycle, Organism organism) {
        String[] traits = {"deeper-memory", "brighter-sense", "quiet-hunger", "rain-wise", "shadow-tuned", "resilient"};
        int index = Math.floorMod(organism.id().hashCode() + cycle + organism.generation(), traits.length);
        return traits[index];
    }

    private Optional<GardenEvent> maybeDescribeChange(Organism before, Organism after, Environment environment, int cycle) {
        if (after.energy() > before.energy()) {
            return Optional.of(new GardenEvent(cycle, "%s gathered energy from the %s garden.".formatted(after.id(), environment.mood())));
        }
        if (after.energy() < before.energy() && after.type().isAnimal()) {
            return Optional.of(new GardenEvent(cycle, "%s spent energy moving through the garden.".formatted(after.id())));
        }
        if (after.curiosity() > before.curiosity()) {
            return Optional.of(new GardenEvent(cycle, "%s became more curious under changing conditions.".formatted(after.id())));
        }
        return Optional.empty();
    }

    private static List<GardenEvent> trimEvents(List<GardenEvent> events) {
        if (events.size() <= MAX_EVENTS) {
            return List.copyOf(events);
        }
        return List.copyOf(events.subList(events.size() - MAX_EVENTS, events.size()));
    }

    private record ReproductionResult(List<Organism> organisms, int nextId) {
    }
}
