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
        long releaserCount = organisms.stream().filter(o -> o.traits().contains("buffer-releaser")).count();
        return rootContribution(releaserCount);
    }

    public int rootContribution(long releaserCount) {
        long fungusCount = countType(OrganismType.FUNGUS);
        long mycelialRootMediatorCount = (fungusCount > 0) ? TraitRegistry.countAnimalTrait(organisms, "mycelial-root-mediator") : 0;

        var context = new RootContributionCalculator.RootContributionContext(
                countType(OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-weaver", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-sharer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "buffer-optimizer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "soil-master", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-recycler", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-translocator", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-synthesizer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-reclaimer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-producer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-pump", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "nutrient-distributor", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "fungal-root-symbiont", OrganismType.ROOT_NETWORK),
                mycelialRootMediatorCount,
                releaserCount,
                environment.nutrients(),
                environment.nutrientBuffer()
        );
        return RootContributionCalculator.calculate(context);
    }

    public long blockedPlantCount() {
        return organisms.stream()
            .filter(o -> o.type().isPlant())
            .filter(o -> o.traits().contains("stressed") ||
                         (o.traits().contains("cautious-breeder") && environment.nutrients() < 10))
            .count();
    }

    public int fungalContribution() {
        return FungalContributionCalculator.calculate(new FungalContributionCalculator.FungalContributionContext(
                countType(OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "nutrient-decomposer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungus-soil-enricher", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-network-connector", OrganismType.FUNGUS),
                TraitRegistry.countPlantTrait(organisms, "fungal-symbiote"),
                TraitRegistry.count(organisms, "fungal-accelerator", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-enhancer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-buffer-stabilizer", OrganismType.FUNGUS),
                TraitRegistry.countAnimalTrait(organisms, "fungal-gardener"),
                TraitRegistry.countAnimalTrait(organisms, "fungal-fertilizer"),
                countType(OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "mycelial-synergizer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "fungal-decomposer-mimic", OrganismType.ROOT_NETWORK)
        ));
    }

    public int fungalAttractorContribution() {
        long fungalAttractorCount = TraitRegistry.count(organisms, "fungal-attractor", OrganismType.ROOT_NETWORK);
        return (fungalAttractorCount > 0 && fungalContribution() > 0) ? 1 : 0;
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
        long releaserCount = TraitRegistry.count(organisms, "buffer-releaser");
        int rootContribution = rootContribution(releaserCount);
        int fungalContribution = fungalContribution();
        
        EnvironmentalUpdateCalculator.EnvironmentalUpdateResult envUpdate = EnvironmentalUpdateCalculator.calculate(organisms, environment, nextCycle, events, rootContribution, fungalContribution);
        Environment nextEnvironment = envUpdate.nextEnvironment();
        List<GardenEvent> nextEvents = envUpdate.updatedEvents();

        int fungalAttractorContribution = fungalAttractorContribution();
        int mossContribution = mossContribution();
        List<Organism> changed = organisms.stream()
                .map(organism -> PassiveChangeCalculator.calculate(organism, environment, nextCycle, nextEvents, fungalContribution, fungalAttractorContribution, mossContribution, organisms))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        FeedingPhaseCalculator.FeedingResult feeding = FeedingPhaseCalculator.calculate(changed, environment, nextCycle, nextEvents);
        
        // Add nutrients and moisture based on deaths
        Environment environmentWithNutrientsAndMoisture = nextEnvironment.withNutrients(feeding.totalNutrientContribution() + feeding.predatorNutrientContribution())
                .withMoisture(feeding.totalMoistureContribution())
                .withNutrientBuffer(nextEnvironment.nutrientBuffer() + feeding.nutrientBufferBoost());
        
        ReproductionCalculator.ReproductionResult reproduction = ReproductionCalculator.calculate(new ReproductionCalculator.ReproductionContext(
                environment, feeding.organisms(), nextCycle, nextId, nextEvents, fungalContribution));
        List<Organism> finalChanged = reproduction.organisms();
        int nextIdentifier = reproduction.nextId();
        
        if (finalChanged.isEmpty()) {
            OrganismType[] plantTypes = {OrganismType.MOSS, OrganismType.SPORE, OrganismType.FERN, OrganismType.FUNGUS};
            OrganismType selected = plantTypes[new java.util.Random().nextInt(plantTypes.length)];
            String id = selected.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-') + "-" + nextIdentifier;
            finalChanged.add(Organism.of(id, selected, 5, 1, "emergency-seed"));
            nextEvents.add(new GardenEvent(nextCycle, "A last emergency %s appeared to keep the garden alive.".formatted(selected.displayName())));
            nextIdentifier++;
        }

        long currentAnimalCount = finalChanged.stream().filter(o -> o.type().isAnimal()).count();
        long currentPlantCount = finalChanged.stream().filter(o -> o.type().isPlant()).count();
        if (currentAnimalCount == 0 && currentPlantCount > 200 && !Boolean.getBoolean("disable.emergency.colonization") && new java.util.Random().nextInt(20) == 0) {
            OrganismType herbivore = OrganismType.BEETLE;
            String id = herbivore.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-') + "-" + nextIdentifier;
            finalChanged.add(Organism.of(id, herbivore, 5, 2, "emergency-colonizer"));
            nextEvents.add(new GardenEvent(nextCycle, "A new %s arrived to colonize the garden.".formatted(herbivore.displayName())));
            nextIdentifier++;
        }

        long herbivoreCount = finalChanged.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.HERBIVORE).count();
        long predatorCount = finalChanged.stream().filter(o -> o.type().kingdom() == OrganismType.Kingdom.PREDATOR).count();
        if (herbivoreCount > 0 && predatorCount < 3 && !Boolean.getBoolean("disable.emergency.colonization") && new java.util.Random().nextInt(20) == 0) {
            OrganismType predator = OrganismType.FOX;
            String id = predator.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-') + "-" + nextIdentifier;
            finalChanged.add(Organism.of(id, predator, 5, 8, "emergency-colonizer"));
            nextEvents.add(new GardenEvent(nextCycle, "A new %s arrived to colonize the garden.".formatted(predator.displayName())));
            nextIdentifier++;
        }
        
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
