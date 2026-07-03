package garden.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    private long countType(OrganismType type) {
        return organisms.stream().filter(o -> o.type() == type).count();
    }

    // Removed local countTrait methods in favor of TraitRegistry.count


    private static final int MAX_EVENTS = 600;

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
        return EnvironmentalDynamicsCalculator.calculateRootContribution(organisms, environment, TraitRegistry.count(organisms, "buffer-releaser"));
    }

    public long blockedPlantCount() {
        return organisms.stream()
            .filter(o -> o.type().isPlant())
            .filter(o -> o.traits().contains("stressed") ||
                         (o.traits().contains("cautious-breeder") && environment.nutrients() < 10))
            .count();
    }

    public int fungalContribution() {
        return EnvironmentalDynamicsCalculator.calculateFungalContribution(organisms, environment);
    }

    public int fungalAttractorContribution() {
        return (TraitRegistry.count(organisms, "fungal-attractor", OrganismType.ROOT_NETWORK) > 0 && fungalContribution() > 0) ? 1 : 0;
    }

    public int mossContribution() {
        return organisms.stream().anyMatch(organism -> organism.type() == OrganismType.MOSS) ? 1 : 0;
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
        EnvironmentalDynamicsCalculator.EnvironmentalDynamicsResult dynamics = EnvironmentalDynamicsCalculator.calculate(new EnvironmentalDynamicsCalculator.EnvironmentalDynamicsContext(organisms, environment, nextCycle, new ArrayList<>(events)));
        EnvironmentalDynamicsCalculator.ContributionResult contribution = dynamics.contribution();
        Environment nextEnvironment = dynamics.nextEnvironment();
        List<GardenEvent> nextEvents = dynamics.updatedEvents();

        List<Organism> changed = PassiveChangeCalculator.calculate(new PassiveChangeCalculator.PassiveChangeContext(environment, nextCycle, nextEvents, contribution, organisms));

        FeedingPhaseCalculator.FeedingResult feeding = FeedingPhaseCalculator.calculate(new FeedingPhaseCalculator.FeedingPhaseContext(changed, environment, nextCycle, nextEvents));
        
        // Add nutrients and moisture based on deaths
        Environment environmentWithNutrientsAndMoisture = nextEnvironment.applyFeeding(
                feeding.totalNutrientContribution(),
                feeding.predatorNutrientContribution(),
                feeding.totalMoistureContribution(),
                feeding.nutrientBufferBoost());
        
        PopulationDynamicsCalculator.PopulationResult population = PopulationDynamicsCalculator.calculate(new PopulationDynamicsCalculator.PopulationContext(
                environment, feeding.organisms(), nextCycle, nextId, nextEvents, contribution.fungalContribution(), new Random()));
        List<Organism> finalChanged = population.organisms();
        int nextIdentifier = population.nextId();
        
        nextEvents.add(new GardenEvent(nextCycle,
                "The garden becomes %s after cycle %d.".formatted(environmentWithNutrientsAndMoisture.mood(), nextCycle)));
        
        return new Garden(nextCycle, nextIdentifier, environmentWithNutrientsAndMoisture, finalChanged, nextEvents);
    }




    private static List<GardenEvent> trimEvents(List<GardenEvent> events) {
        if (events.size() <= MAX_EVENTS) {
            return List.copyOf(events);
        }
        return List.copyOf(events.subList(events.size() - MAX_EVENTS, events.size()));
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

}
