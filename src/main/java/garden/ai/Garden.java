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
        long plantCount = organisms.stream().filter(organism -> organism.type().isPlant()).count();
        long animalCount = organisms.stream().filter(organism -> organism.type().isAnimal()).count();
        
        long mossCount = countType(OrganismType.MOSS);
        long fernCount = countType(OrganismType.FERN);
        long sporeCount = countType(OrganismType.SPORE);
        long rootNetworkCount = countType(OrganismType.ROOT_NETWORK);
        long fungusCount = countType(OrganismType.FUNGUS);
        long mossConserverCount = TraitRegistry.count(organisms, "nutrient-conserver", OrganismType.MOSS);
        long mossScavengerCount = TraitRegistry.count(organisms, "moss-nutrient-scavenger", OrganismType.MOSS);
        long fernConserverCount = TraitRegistry.count(organisms, "nutrient-conserver", OrganismType.FERN);
        long mobilizerCount = TraitRegistry.count(organisms, "nutrient-mobilizer");
        long fungalNutrientMobilizerCount = TraitRegistry.count(organisms, "fungal-nutrient-mobilizer", OrganismType.FUNGUS);
        long releaserCount = TraitRegistry.count(organisms, "buffer-releaser");
        long recyclerCount = TraitRegistry.count(organisms, "nutrient-recycler");
        long distributorCount = TraitRegistry.count(organisms, "nutrient-distributor");
        long demandRegulatorCount = TraitRegistry.count(organisms, "nutrient-demand-regulator");
        long siphonCount = TraitRegistry.count(organisms, "buffer-siphon");
        
        int reductionFactor = environment.nutrients() < 10 ? 1 : 5;
        int plantConsumptionReduction = (int) ((mossConserverCount + mossScavengerCount + fernConserverCount) / reductionFactor);
        Environment nextEnvironment = environment.next(nextCycle, (int) plantCount, (int) animalCount, rootContribution(releaserCount), fungalContribution(), plantConsumptionReduction, (int) demandRegulatorCount / 2, (int) (mobilizerCount + fungalNutrientMobilizerCount), (int) releaserCount, (int) siphonCount);
        List<GardenEvent> nextEvents = new ArrayList<>(events);
        int production = 2 + (int)animalCount / 2;
        int mossConsumption = (int)Math.max(0, mossCount / 5 - (mossConserverCount + mossScavengerCount) / reductionFactor);
        int fernConsumption = (int)Math.max(0, fernCount / 5 - fernConserverCount / reductionFactor);
        int consumption = Math.max(0, mossConsumption + fernConsumption - (int) demandRegulatorCount / 2);
        nextEvents.add(new GardenEvent(nextCycle, "Nutrient change breakdown: prod=%d, cons=%d (moss=%d, fern=%d, root-reduction=%d)".formatted(production, consumption, mossConsumption, fernConsumption, (int) demandRegulatorCount / 2)));
        nextEvents.add(new GardenEvent(nextCycle, "Plant breakdown: moss=%d, fern=%d, spore=%d, roots=%d, fungus=%d".formatted(mossCount, fernCount, sporeCount, rootNetworkCount, fungusCount)));

        if (nextEnvironment.nutrients() < environment.nutrients()) {
            nextEvents.add(new GardenEvent(nextCycle, "Nutrients are depleted by the plant population."));
        }
        if (nextEnvironment.nutrientBuffer() < environment.nutrientBuffer()) {
            nextEvents.add(new GardenEvent(nextCycle, "Nutrients released from the buffer."));
        }
        if (nextEnvironment.nutrientBuffer() > environment.nutrientBuffer()) {
            nextEvents.add(new GardenEvent(nextCycle, "The nutrient buffer is accumulating."));
        }
        int baseReleaseRate = environment.nutrients() < 5 ? 2 : (environment.nutrients() < 10 ? 5 : 10);
        int effectiveReleaseRate = Math.max(1, baseReleaseRate - (int)(mobilizerCount + fungalNutrientMobilizerCount) - (int)releaserCount - (int)recyclerCount - (int)distributorCount);
        int releasedFromBuffer = environment.nutrientBuffer() / effectiveReleaseRate;
        nextEvents.add(new GardenEvent(nextCycle, "Buffer release stats: baseRate=%d, mobilizers=%d, releasers=%d, recyclers=%d, distributors=%d, effectiveRate=%d, released=%d".formatted(baseReleaseRate, mobilizerCount + fungalNutrientMobilizerCount, releaserCount, recyclerCount, distributorCount, effectiveReleaseRate, releasedFromBuffer)));
        
        int availableNutrients = environment.nutrients() + releasedFromBuffer;
        if (consumption > availableNutrients) {
            nextEvents.add(new GardenEvent(nextCycle, "Nutrient scarcity is bottlenecking growth (consumption=%d, available=%d).".formatted(consumption, availableNutrients)));
        }

        if (plantCount > 200 && nextEnvironment.nutrients() < 10) {
            nextEvents.add(new GardenEvent(nextCycle, "High population pressure is straining nutrient reserves."));
        }
        if (nextEnvironment.nutrientBuffer() < 10) {
            nextEvents.add(new GardenEvent(nextCycle, "The nutrient buffer is near exhaustion."));
        }
        int fungalContribution = fungalContribution();
        int fungalAttractorContribution = fungalAttractorContribution();
        int mossContribution = mossContribution();
        List<Organism> changed = organisms.stream()
                .map(organism -> passiveChange(organism, environment, nextCycle, nextEvents, fungalContribution, fungalAttractorContribution, mossContribution))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        FeedingResult feeding = feedingPhase(changed, nextCycle, nextEvents);
        
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

    private Organism passiveChange(Organism organism, Environment environment, int cycle, List<GardenEvent> events, int fungalContribution, int fungalAttractorContribution, int mossContribution) {
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
            // Trait-based growth
            for (String trait : organism.traits()) {
                TraitRegistry.PlantGrowthEffect effect = TraitRegistry.getPlantGrowthEffect(trait, cycle, organism, environment, organisms, fungalContribution);
                if (effect != null) {
                    growth += effect.growthChange();
                    if (effect.event() != null) {
                        events.add(effect.event());
                    }
                }
            }
            if (organism.type() == OrganismType.ROOT_NETWORK && changed.traits().contains("fungal-root-symbiont") && fungalContribution > 0) {
                events.add(new GardenEvent(cycle, "%s benefited from its fungal symbiont.".formatted(changed.id())));
            }
            if (organism.type() == OrganismType.SPORE && environment.light() < 45) {
                changed = changed.withCuriosity(changed.curiosity() + 2);
            }
            changed = changed.withEnergy(changed.energy() + growth);
        } else {
            MetabolismCalculator.MetabolicResult result = MetabolismCalculator.calculate(cycle, changed, environment, new MetabolismCalculator.ContributionContext(mossContribution, fungalContribution, fungalAttractorContribution));
            events.addAll(result.events());
            changed = changed.withEnergy(changed.energy() + result.energyBonus() - result.metabolism())
                    .withCuriosity(changed.curiosity() + (cycle % 4 == 0 ? 1 : 0));
        }

        maybeDescribeChange(organism, changed, environment, cycle).ifPresent(events::add);
        
        if (changed.energy() <= 2 && changed.energy() > 0) {
            events.add(new GardenEvent(cycle, "%s is at a critical energy level.".formatted(changed.id())));
        }

        boolean isResilient = changed.traits().contains("resilient");
        boolean isDormant = changed.traits().contains("dormancy") && environment.nutrients() < 15;
        boolean isDeepRooting = changed.traits().contains("deep-rooting") && environment.moisture() < 30;
        boolean isStressResilient = changed.traits().contains("stress-resilient");
        boolean isStressAvoidant = changed.traits().contains("stress-avoidance");

        if (organism.type().isPlant()) {
            if (!environment.favorsPlants() && !isResilient && !isDormant && !isDeepRooting && !isStressResilient && !isStressAvoidant) {
                if (environment.nutrients() == 0) {
                    changed = changed.withEnergy(Math.max(0, changed.energy() - 1));
                    events.add(new GardenEvent(cycle, "%s lost energy due to environmental stress.".formatted(changed.id())));
                }
                changed = changed.withTrait("stressed");
            } else {
                changed = changed.withoutTrait("stressed");
            }
        } else if (organism.type().isAnimal()) {
            if (environment.nutrients() < 25 && !isResilient && !isDormant) {
                changed = changed.withTrait("starving");
            } else {
                changed = changed.withoutTrait("starving");
            }
        }

        return maybeMutate(changed, cycle, events);
    }

    private FeedingResult feedingPhase(List<Organism> organisms, int cycle, List<GardenEvent> events) {
        List<Organism> mutable = new ArrayList<>(organisms);
        mutable.sort(Comparator.comparing((Organism organism) -> organism.type().kingdom().ordinal())
                .thenComparing(Organism::id));

        int predatorNutrientContribution = 0;
        long rootNetworkCount = organisms.stream().filter(o -> o.type() == OrganismType.ROOT_NETWORK).count();
        for (int hunterIndex = 0; hunterIndex < mutable.size(); hunterIndex++) {
            Organism hunter = mutable.get(hunterIndex);
            if (!hunter.type().isAnimal() || hunter.energy() <= 0) {
                continue;
            }
            if (hunter.traits().contains("cautious-feeder") && hunter.energy() > 15) {
                events.add(new GardenEvent(cycle, "%s skipped feeding to conserve energy.".formatted(hunter.id())));
                continue;
            }
            Optional<Integer> preyIndex = findPreyIndex(mutable, hunter, hunterIndex, events);
            if (preyIndex.isEmpty()) {
                if (hunter.traits().contains("starving")) {
                    events.add(new GardenEvent(cycle, "%s is desperately searching for prey in the scarce garden.".formatted(hunter.id())));
                } else if (environment.nutrients() < 25 && (hunter.id().hashCode() + cycle) % 10 == 0) {
                    events.add(new GardenEvent(cycle, "In the hungry garden, %s could not find prey (looking for: %s).".formatted(hunter.id(), hunter.type().prey())));
                }
                continue;
            }

            int index = preyIndex.get();
            Organism prey = mutable.get(index);
            
            FeedingBiteCalculator.FeedingBiteResult biteResult = FeedingBiteCalculator.calculate(new FeedingBiteCalculator.FeedingBiteContext(hunter, prey, environment, cycle, rootNetworkCount));
            int bite = biteResult.biteSize();
            events.addAll(biteResult.events());
            
            if (hunter.type() == OrganismType.FOX) {
                predatorNutrientContribution += 1;
            }
            Organism fedHunter = hunter.withEnergy(hunter.energy() + bite).withTrait("fed-" + cycle);
            Organism weakenedPrey = prey.withEnergy(prey.energy() - bite);
            mutable.set(hunterIndex, fedHunter);
            mutable.set(index, weakenedPrey);
            events.add(new GardenEvent(cycle, "%s fed on %s.".formatted(hunter.id(), prey.id())));
        }

        int totalNutrientContribution = 0;
        int totalMoistureContribution = 0;
        int deadOrganisms = 0;
        int moistureRetainers = 0;
        int mycelialDistributors = 0;
        List<Organism> survivors = new ArrayList<>();
        for (Organism organism : mutable) {
            if (organism.energy() > 0) {
                survivors.add(organism);
            } else {
                if (organism.traits().contains("stressed")) {
                    events.add(new GardenEvent(cycle, "%s was culled due to chronic environmental stress.".formatted(organism.id())));
                    if (organism.traits().contains("nutrient-recycler")) {
                        totalNutrientContribution += 5;
                    }
                }
                totalNutrientContribution += organism.nutrientValue();
                deadOrganisms++;
                if (organism.traits().contains("moisture-retainer") && organism.type() == OrganismType.MOSS) {
                    totalMoistureContribution += 5;
                    moistureRetainers++;
                }
                if (organism.traits().contains("mycelial-distributor")) {
                    mycelialDistributors++;
                }
            }
        }
        if (deadOrganisms > 0) {
            events.add(new GardenEvent(cycle, "%d organisms returned %d nutrients to the soil.".formatted(deadOrganisms, totalNutrientContribution)));
            if (moistureRetainers > 0) {
                events.add(new GardenEvent(cycle, "%d moisture-retainers returned %d moisture to the soil.".formatted(moistureRetainers, totalMoistureContribution)));
            }
            if (mycelialDistributors > 0) {
                events.add(new GardenEvent(cycle, "%d mycelial-distributors distributed nutrients to the fungal network.".formatted(mycelialDistributors)));
            }
        }
        survivors.sort(Comparator.comparing(Organism::id));
        
        // Final adjustment to the feeding result to include nutrient buffer boost
        int bufferBoost = 0;
        for (Organism organism : mutable) {
            if (organism.energy() <= 0 && organism.traits().contains("mycelial-distributor")) {
                long fungusCount = organisms.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
                if (fungusCount > 0) {
                    bufferBoost += 5;
                }
            }
            if (organism.energy() > 0 && organism.traits().contains("buffer-stabilizer")) {
                long fungusCount = organisms.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
                if (fungusCount > 0) {
                    bufferBoost += 2;
                }
            }
        }

        return new FeedingResult(survivors, totalNutrientContribution, totalMoistureContribution, bufferBoost, deadOrganisms, predatorNutrientContribution);
        }

        private record FeedingResult(List<Organism> organisms, int totalNutrientContribution, int totalMoistureContribution, int nutrientBufferBoost, int culledPlantCount, int predatorNutrientContribution) {
        }


    private Optional<Integer> findPreyIndex(List<Organism> organisms, Organism hunter, int hunterIndex, List<GardenEvent> events) {
        boolean nutrientScout = hunter.traits().contains("nutrient-scout");
        boolean preyTracker = hunter.traits().contains("prey-tracker");
        boolean resourceTracker = hunter.traits().contains("resource-tracker");
        boolean mycelialNetworkScout = hunter.traits().contains("mycelial-network-scout");

        boolean stealthHunter = hunter.traits().contains("stealth-hunter");

        java.util.function.Predicate<Organism> isValidPrey = candidate -> {
            if (candidate.energy() <= 0 || !hunter.type().canEat(candidate.type())) return false;
            if (!stealthHunter) {
                if (candidate.traits().contains("shadow-stepper") && (candidate.id().hashCode() + cycle) % 2 == 0) return false;
                if (candidate.traits().contains("camouflaged") && (candidate.id().hashCode() + cycle) % 3 == 0) return false;
            }
            return true;
        };

        // Pass 1: Look for nutrient-hoarder if hunter is a scout or resource-tracker
        if (nutrientScout || resourceTracker) {
            for (int i = 0; i < organisms.size(); i++) {
                if (i == hunterIndex) continue;
                Organism candidate = organisms.get(i);
                if (isValidPrey.test(candidate) && candidate.traits().contains("nutrient-hoarder")) {
                    return Optional.of(i);
                }
            }
        }
        
        // New Pass: Look for prey near fungal network if hunter is mycelial-network-scout
        if (mycelialNetworkScout && fungalContribution() > 0) {
            for (int i = 0; i < organisms.size(); i++) {
                if (i == hunterIndex) continue;
                Organism candidate = organisms.get(i);
                // For simplicity in this simulation, prioritize any prey if fungal network exists
                if (isValidPrey.test(candidate)) {
                    events.add(new GardenEvent(cycle, "%s scouted prey using the fungal network.".formatted(hunter.id())));
                    return Optional.of(i);
                }
            }
        }

        // Pass 2: Look for highest energy prey if hunter is tracker
        if (preyTracker) {
            int maxEnergy = -1;
            int bestIndex = -1;
            for (int i = 0; i < organisms.size(); i++) {
                if (i == hunterIndex) continue;
                Organism candidate = organisms.get(i);
                if (isValidPrey.test(candidate) && candidate.energy() > maxEnergy) {
                    maxEnergy = candidate.energy();
                    bestIndex = i;
                }
            }
            if (bestIndex != -1) {
                events.add(new GardenEvent(cycle, "%s tracked the most energetic prey.".formatted(hunter.id())));
                return Optional.of(bestIndex);
            }
        }

        // Pass 3: Look for any valid prey
        for (int i = 0; i < organisms.size(); i++) {
            if (i == hunterIndex) continue;
            Organism candidate = organisms.get(i);
            if (isValidPrey.test(candidate)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }


    private Organism maybeMutate(Organism organism, int cycle, List<GardenEvent> events) {
        if ((organism.energy() + organism.curiosity() + cycle + organism.generation()) % 11 != 0) {
            return organism;
        }
        String trait;
        if (organism.type() == OrganismType.ROOT_NETWORK && organism.traits().contains("stressed") && (organism.id().hashCode() + cycle) % 5 == 0) {
            trait = "fungal-symbiote";
        } else {
            trait = TraitRegistry.getMutationTrait(cycle, organism);
        }
        Organism changed = organism.withTrait(trait).withCuriosity(organism.curiosity() + 1);
        events.add(new GardenEvent(cycle, "%s adapted a %s trait.".formatted(organism.id(), trait)));
        return changed;
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

}
