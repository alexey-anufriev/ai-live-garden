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
        return new Garden(0, 8, new Environment(50, 64, 43, 58, 50), organisms, events);
    }

    public int rootContribution() {
        long rootNetworkCount = organisms.stream().filter(organism -> organism.type() == OrganismType.ROOT_NETWORK).count();
        long nutrientWeaverCount = organisms.stream().filter(organism -> organism.type() == OrganismType.ROOT_NETWORK && organism.traits().contains("nutrient-weaver")).count();
        long nutrientSharerCount = organisms.stream().filter(organism -> organism.type() == OrganismType.ROOT_NETWORK && organism.traits().contains("nutrient-sharer")).count();
        long bufferOptimizerCount = organisms.stream().filter(organism -> organism.type() == OrganismType.ROOT_NETWORK && organism.traits().contains("buffer-optimizer")).count();
        long soilMasterCount = organisms.stream().filter(organism -> organism.type() == OrganismType.ROOT_NETWORK && organism.traits().contains("soil-master")).count();
        
        if (environment.nutrients() < 5) {
            return (int) (rootNetworkCount * 10 + nutrientWeaverCount * 10 + nutrientSharerCount * 20 + bufferOptimizerCount * 20 + soilMasterCount * 30);
        } else if (environment.nutrients() < 10) {
            return (int) (rootNetworkCount * 8 + nutrientWeaverCount * 8 + nutrientSharerCount * 16 + bufferOptimizerCount * 16 + soilMasterCount * 24);
        } else if (environment.nutrients() < 25) {
            return (int) (rootNetworkCount * 4 + nutrientWeaverCount * 4 + nutrientSharerCount * 8 + bufferOptimizerCount * 8 + soilMasterCount * 12);
        } else {
            return (int) (rootNetworkCount / 2 + nutrientWeaverCount + nutrientSharerCount * 2 + bufferOptimizerCount * 2 + soilMasterCount * 4);
        }
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
        
        Environment nextEnvironment = environment.next(nextCycle, (int) plantCount, (int) animalCount, rootContribution());
        List<GardenEvent> nextEvents = new ArrayList<>(events);
        if (nextEnvironment.nutrients() < environment.nutrients()) {
            nextEvents.add(new GardenEvent(nextCycle, "Nutrients are depleted by the plant population."));
        }
        if (plantCount > 200 && nextEnvironment.nutrients() < 10) {
            nextEvents.add(new GardenEvent(nextCycle, "High population pressure is straining nutrient reserves."));
        }
        if (nextEnvironment.nutrientBuffer() < 10) {
            nextEvents.add(new GardenEvent(nextCycle, "The nutrient buffer is near exhaustion."));
        }
        List<Organism> changed = organisms.stream()
                .map(organism -> passiveChange(organism, environment, nextCycle, nextEvents))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        FeedingResult feeding = feedingPhase(changed, nextCycle, nextEvents);
        
        // Add nutrients based on deaths
        Environment environmentWithNutrients = nextEnvironment.withNutrients(feeding.totalNutrientContribution());
        
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
            if (organism.type() == OrganismType.ROOT_NETWORK && environment.nutrients() < 25) {
                growth += 1;
            }
            if (organism.type() == OrganismType.MOSS && environment.moisture() > 60) {
                growth += 1;
            }
            if (changed.traits().contains("water-seeker") && environment.moisture() < 50) {
                growth += 1;
            }
            if (changed.traits().contains("sun-lover") && environment.light() > 60) {
                growth += 1;
            }
            if (changed.traits().contains("rain-collector") && environment.moisture() < 40) {
                growth += 1;
            }
            if (organism.type() == OrganismType.FERN && changed.traits().contains("hardy") && environment.warmth() > 50) {
                growth += 1;
            }
            if (organism.type() == OrganismType.SPORE && environment.light() < 45) {
                changed = changed.withCuriosity(changed.curiosity() + 2);
            }
            if (changed.traits().contains("nutrient-efficient") && environment.nutrients() < 30) {
                growth += 1;
            }
            if (changed.traits().contains("buffer-tapper") && environment.nutrients() < 5 && environment.nutrientBuffer() > 0) {
                growth += 2;
                events.add(new GardenEvent(cycle, "%s tapped the nutrient buffer.".formatted(changed.id())));
            }
            if (changed.traits().contains("buffer-resonator") && environment.nutrientBuffer() > 0) {
                growth += 1;
                events.add(new GardenEvent(cycle, "%s utilized the nutrient buffer.".formatted(changed.id())));
            }
            if (changed.traits().contains("resilient")) {
                growth -= 1;
            }
            changed = changed.withEnergy(changed.energy() + growth);
        } else {
            int metabolism = organism.type().metabolism();
            if (changed.traits().contains("resilient")) {
                metabolism += 1;
            }
            if (changed.traits().contains("dormancy") && environment.nutrients() < 15) {
                metabolism = Math.max(0, metabolism - 2);
            }
            if (changed.traits().contains("metabolic-efficiency")) {
                metabolism = Math.max(0, metabolism - 1);
            }
            if (changed.traits().contains("buffer-scavenger") && environment.nutrientBuffer() > 0) {
                metabolism = Math.max(0, metabolism - 1);
                events.add(new GardenEvent(cycle, "%s utilized the nutrient buffer.".formatted(changed.id())));
            }
            changed = changed.withEnergy(changed.energy() - metabolism)
                    .withCuriosity(changed.curiosity() + (cycle % 4 == 0 ? 1 : 0));
        }

        maybeDescribeChange(organism, changed, environment, cycle).ifPresent(events::add);
        
        if (changed.energy() <= 2 && changed.energy() > 0) {
            events.add(new GardenEvent(cycle, "%s is at a critical energy level.".formatted(changed.id())));
        }

        boolean isResilient = changed.traits().contains("resilient");
        boolean isDormant = changed.traits().contains("dormancy") && environment.nutrients() < 15;
        boolean isDeepRooting = changed.traits().contains("deep-rooting") && environment.moisture() < 30;

        if (organism.type().isPlant() && !environment.favorsPlants() && !isResilient && !isDormant && !isDeepRooting) {
            changed = changed.withTrait("stressed");
        } else if (organism.type().isAnimal() && environment.nutrients() < 25 && !isResilient && !isDormant) {
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
                if (environment.nutrients() < 25 && (hunter.id().hashCode() + cycle) % 5 == 0) {
                    events.add(new GardenEvent(cycle, "In the hungry garden, %s could not find prey.".formatted(hunter.id())));
                }
                continue;
            }

            int index = preyIndex.get();
            Organism prey = mutable.get(index);
            int bite = hunter.type() == OrganismType.FOX ? 3 : 2;
            if (hunter.traits().contains("nutrient-finder")) {
                bite += 1;
            }
            if (hunter.traits().contains("scavenger") && environment.nutrients() < 25) {
                bite += 1;
            }
            if (hunter.traits().contains("gentle-feeder")) {
                bite = Math.max(1, bite - 1);
            }
            Organism fedHunter = hunter.withEnergy(hunter.energy() + bite).withTrait("fed-" + cycle);
            Organism weakenedPrey = prey.withEnergy(prey.energy() - bite);
            mutable.set(hunterIndex, fedHunter);
            mutable.set(index, weakenedPrey);
            events.add(new GardenEvent(cycle, "%s fed on %s.".formatted(hunter.id(), prey.id())));
        }

        int totalNutrientContribution = 0;
        List<Organism> survivors = new ArrayList<>();
        for (Organism organism : mutable) {
            if (organism.energy() > 0) {
                survivors.add(organism);
            } else {
                events.add(new GardenEvent(cycle, "%s (%d nutrients) returned to the soil.".formatted(organism.id(), organism.nutrientValue())));
                totalNutrientContribution += organism.nutrientValue();
            }
        }
        survivors.sort(Comparator.comparing(Organism::id));
        return new FeedingResult(survivors, totalNutrientContribution);
    }

    private record FeedingResult(List<Organism> organisms, int totalNutrientContribution) {
    }

    private Optional<Integer> findPreyIndex(List<Organism> organisms, Organism hunter, int hunterIndex) {
        boolean nutrientScout = hunter.traits().contains("nutrient-scout");

        java.util.function.Predicate<Organism> isValidPrey = candidate -> {
            if (candidate.energy() <= 0 || !hunter.type().canEat(candidate.type())) return false;
            if (candidate.traits().contains("shadow-stepper") && (candidate.id().hashCode() + cycle) % 2 == 0) return false;
            return true;
        };

        // Pass 1: Look for nutrient-hoarder if hunter is a scout
        if (nutrientScout) {
            for (int i = 0; i < organisms.size(); i++) {
                if (i == hunterIndex) continue;
                Organism candidate = organisms.get(i);
                if (isValidPrey.test(candidate) && candidate.traits().contains("nutrient-hoarder")) {
                    return Optional.of(i);
                }
            }
        }

        // Pass 2: Look for any valid prey
        for (int i = 0; i < organisms.size(); i++) {
            if (i == hunterIndex) continue;
            Organism candidate = organisms.get(i);
            if (isValidPrey.test(candidate)) {
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
        String[] traits = {"deeper-memory", "brighter-sense", "quiet-hunger", "rain-wise", "shadow-tuned", "resilient", "sun-lover", "rain-collector", "nutrient-finder", "nutrient-efficient", "shadow-stepper", "hardy", "water-seeker", "dormancy", "nutrient-weaver", "metabolic-efficiency", "scavenger", "nutrient-sharer", "buffer-resonator", "buffer-scavenger", "nutrient-hoarder", "nutrient-scout", "soil-master", "deep-rooting", "buffer-optimizer", "buffer-tapper"};
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
