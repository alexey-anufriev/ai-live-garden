package garden.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Consolidated calculator for organism-level interactions: 
 * metabolism, stress, feeding, reproduction, and colonization.
 */
public class OrganismInteractionCalculator {

    // --- Passive Change / Metabolic Logic ---

    public record PassiveChangeContext(
            Environment environment,
            int cycle,
            List<GardenEvent> events,
            EnvironmentalDynamicsCalculator.ContributionResult contribution,
            List<Organism> allOrganisms
    ) {}

    public record ContributionContext(int moss, int fungal, int fungalAttractor) {}
    public record MetabolicResult(int metabolism, int energyBonus, List<GardenEvent> events) {}
    public record StressResult(boolean isStressed, int energyLoss, Optional<GardenEvent> event) {}

    public static List<Organism> calculatePassiveChanges(PassiveChangeContext context) {
        return context.allOrganisms().stream()
                .map(organism -> calculateSingle(organism, context))
                .collect(Collectors.toList());
    }

    private static Organism calculateSingle(Organism organism, PassiveChangeContext context) {
        Organism changed = organism;
        if (organism.type().isPlant()) {
            int growth = context.environment().favorsPlants() ? 2 : 0;
            if (organism.type() == OrganismType.ROOT_NETWORK && context.environment().nutrients() > 45) {
                growth += 1;
            }
            if (organism.type() == OrganismType.ROOT_NETWORK && context.environment().nutrients() < 25) {
                growth += 1;
            }
            if (organism.type() == OrganismType.MOSS && context.environment().moisture() > 60) {
                growth += 1;
            }
            // Trait-based growth
            for (String trait : organism.traits()) {
                TraitRegistry.PlantGrowthEffect effect = TraitRegistry.getPlantGrowthEffect(trait, context.cycle(), organism, context.environment(), context.allOrganisms(), context.contribution().fungalContribution());
                if (effect != null) {
                    growth += effect.growthChange();
                    if (effect.event() != null) {
                        context.events().add(effect.event());
                    }
                }
            }
            if (organism.type() == OrganismType.ROOT_NETWORK && changed.traits().contains("fungal-root-symbiont") && context.contribution().fungalContribution() > 0) {
                context.events().add(new GardenEvent(context.cycle(), "%s benefited from its fungal symbiont.".formatted(changed.id())));
            }
            if (organism.type() == OrganismType.SPORE && context.environment().light() < 45) {
                changed = changed.withCuriosity(changed.curiosity() + 2);
            }
            changed = changed.withEnergy(changed.energy() + growth);
        } else {
            MetabolicResult result = calculateMetabolism(context.cycle(), changed, context.environment(), new ContributionContext(context.contribution().mossContribution(), context.contribution().fungalContribution(), context.contribution().fungalAttractorContribution()));
            context.events().addAll(result.events());
            changed = changed.withEnergy(changed.energy() + result.energyBonus() - result.metabolism())
                    .withCuriosity(changed.curiosity() + (context.cycle() % 4 == 0 ? 1 : 0));
        }

        maybeDescribeChange(organism, changed, context.environment(), context.cycle()).ifPresent(context.events()::add);
        
        if (changed.energy() <= 2 && changed.energy() > 0) {
            context.events().add(new GardenEvent(context.cycle(), "%s is at a critical energy level.".formatted(changed.id())));
        }

        if (organism.type().isPlant()) {
            StressResult stress = calculatePlantStressResult(changed, context.environment(), context.cycle(), context.allOrganisms());
            if (stress.isStressed()) {
                changed = changed.withEnergy(Math.max(0, changed.energy() - stress.energyLoss()));
                stress.event().ifPresent(context.events()::add);
                changed = changed.withTrait("stressed");
            } else {
                changed = changed.withoutTrait("stressed");
            }
        } else if (organism.type().isAnimal()) {
            if (isAnimalStarving(changed, context.environment(), context.allOrganisms())) {
                changed = changed.withTrait("starving");
            } else {
                changed = changed.withoutTrait("starving");
            }
        }

        return maybeMutate(changed, context.cycle(), context.events());
    }

    public static MetabolicResult calculateMetabolism(int cycle, Organism organism, Environment environment, ContributionContext contributions) {
        int metabolism = organism.type().metabolism();
        int energyBonus = 0;
        List<GardenEvent> events = new ArrayList<>();

        for (String trait : organism.traits()) {
            TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect(trait, cycle, organism, environment, contributions.fungal(), contributions.moss());
            if (effect != null) {
                metabolism = Math.max(0, metabolism + effect.metabolismChange());
                energyBonus += effect.energyBonus();
                if (effect.event() != null) {
                    events.add(effect.event());
                }
            }
        }

        if (contributions.fungalAttractor() > 0) {
            energyBonus += 1;
            events.add(new GardenEvent(cycle, "%s was attracted to a fungal-rich area.".formatted(organism.id())));
        }
        return new MetabolicResult(metabolism, energyBonus, events);
    }

    public static StressResult calculatePlantStressResult(Organism organism, Environment environment, int cycle, List<Organism> allOrganisms) {
        if (!isPlantStressed(organism, environment, allOrganisms)) {
            return new StressResult(false, 0, Optional.empty());
        }

        int energyLoss = 0;
        Optional<GardenEvent> event = Optional.empty();

        if (environment.nutrients() == 0) {
            energyLoss = 1;
            event = Optional.of(new GardenEvent(cycle, "%s lost energy due to environmental stress.".formatted(organism.id())));
        } else if (allOrganisms.stream().filter(o -> o.type().isPlant()).count() > 5000) {
            energyLoss = 1;
            event = Optional.of(new GardenEvent(cycle, "%s lost energy due to overcrowding.".formatted(organism.id())));
        }

        return new StressResult(true, energyLoss, event);
    }

    public static boolean isPlantStressed(Organism organism, Environment environment, List<Organism> allOrganisms) {
        if (!organism.type().isPlant()) return false;
        boolean isResilient = organism.traits().contains("resilient");
        boolean isDormant = organism.traits().contains("dormancy") && environment.nutrients() < 15;
        boolean isDeepRooting = organism.traits().contains("deep-rooting") && environment.moisture() < 30;
        boolean isStressResilient = organism.traits().contains("stress-resilient");
        boolean isStressAvoidant = organism.traits().contains("stress-avoidance");
        boolean crowded = allOrganisms.stream().filter(o -> o.type().isPlant()).count() > 5000;
        return (!environment.favorsPlants() || crowded) && !isResilient && !isDormant && !isDeepRooting && !isStressResilient && !isStressAvoidant;
    }

    public static boolean isAnimalStarving(Organism organism, Environment environment, List<Organism> allOrganisms) {
        if (!organism.type().isAnimal()) return false;
        boolean isResilient = organism.traits().contains("resilient");
        boolean isDormant = organism.traits().contains("dormancy");
        boolean overcrowded = allOrganisms.stream().filter(o -> o.type().isAnimal()).count() > 2500;
        return ((environment.nutrients() + environment.nutrientBuffer() / 2) < 25 || overcrowded) && !isResilient && !isDormant;
    }

    private static Organism maybeMutate(Organism organism, int cycle, List<GardenEvent> events) {
        if ((organism.energy() + organism.curiosity() + cycle + organism.generation()) % 11 != 0) return organism;
        String trait;
        if (organism.type() == OrganismType.ROOT_NETWORK && organism.traits().contains("stressed") && (organism.id().hashCode() + cycle) % 5 == 0) {
            trait = "fungal-symbiote";
        } else {
            trait = TraitRegistry.getMutationTrait(cycle, organism, organism.type());
        }
        Organism changed = organism.withTrait(trait).withCuriosity(organism.curiosity() + 1);
        events.add(new GardenEvent(cycle, "%s adapted a %s trait.".formatted(organism.id(), trait)));
        return changed;
    }

    private static Optional<GardenEvent> maybeDescribeChange(Organism before, Organism after, Environment environment, int cycle) {
        if (after.energy() > before.energy()) return Optional.of(new GardenEvent(cycle, "%s gathered energy from the %s garden.".formatted(after.id(), environment.mood())));
        if (after.energy() < before.energy() && after.type().isAnimal()) return Optional.of(new GardenEvent(cycle, "%s spent energy moving through the garden.".formatted(after.id())));
        if (after.curiosity() > before.curiosity()) return Optional.of(new GardenEvent(cycle, "%s became more curious under changing conditions.".formatted(after.id())));
        return Optional.empty();
    }

    // --- Feeding Phase Logic ---

    public record FeedingResult(List<Organism> organisms, int totalNutrientContribution, int totalMoistureContribution, int nutrientBufferBoost, int culledPlantCount, int predatorNutrientContribution) {}
    public record FeedingPhaseContext(List<Organism> organisms, Environment environment, int cycle, List<GardenEvent> events) {}

    public static FeedingResult calculateFeeding(FeedingPhaseContext context) {
        List<Organism> organisms = context.organisms();
        Environment environment = context.environment();
        int cycle = context.cycle();
        List<GardenEvent> events = context.events();
        List<Organism> mutable = new ArrayList<>(organisms);
        mutable.sort(Comparator.comparing((Organism organism) -> organism.type().kingdom().ordinal())
                .thenComparing(Organism::id));

        int predatorNutrientContribution = 0;
        long rootNetworkCount = organisms.stream().filter(o -> o.type() == OrganismType.ROOT_NETWORK).count();
        
        for (int hunterIndex = 0; hunterIndex < mutable.size(); hunterIndex++) {
            Organism hunter = mutable.get(hunterIndex);
            if (!hunter.type().isAnimal() || hunter.energy() <= 0) continue;
            if (hunter.traits().contains("cautious-feeder") && hunter.energy() > 15) {
                events.add(new GardenEvent(cycle, "%s skipped feeding to conserve energy.".formatted(hunter.id())));
                continue;
            }
            Optional<Integer> preyIndex = findPreyIndex(mutable, hunter, hunterIndex, cycle, environment, events);
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
            FeedingBiteResult biteResult = calculateBite(hunter, prey, environment, cycle, rootNetworkCount);
            int bite = biteResult.biteSize();
            events.addAll(biteResult.events());
            
            if (hunter.type() == OrganismType.FOX) predatorNutrientContribution += 1;
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
            if (organism.energy() > 0) survivors.add(organism);
            else {
                if (organism.traits().contains("stressed")) {
                    events.add(new GardenEvent(cycle, "%s was culled due to chronic environmental stress.".formatted(organism.id())));
                    if (organism.traits().contains("nutrient-recycler")) totalNutrientContribution += 5;
                }
                totalNutrientContribution += organism.nutrientValue();
                deadOrganisms++;
                if (organism.traits().contains("moisture-retainer") && organism.type() == OrganismType.MOSS) {
                    totalMoistureContribution += 5;
                    moistureRetainers++;
                }
                if (organism.traits().contains("mycelial-distributor")) mycelialDistributors++;
            }
        }
        if (deadOrganisms > 0) {
            events.add(new GardenEvent(cycle, "%d organisms returned %d nutrients to the soil.".formatted(deadOrganisms, totalNutrientContribution)));
            if (moistureRetainers > 0) events.add(new GardenEvent(cycle, "%d moisture-retainers returned %d moisture to the soil.".formatted(moistureRetainers, totalMoistureContribution)));
            if (mycelialDistributors > 0) events.add(new GardenEvent(cycle, "%d mycelial-distributors distributed nutrients to the fungal network.".formatted(mycelialDistributors)));
        }
        survivors.sort(Comparator.comparing(Organism::id));
        
        int bufferBoost = 0;
        for (Organism organism : mutable) {
            if (organism.energy() <= 0 && organism.traits().contains("mycelial-distributor")) {
                long fungusCount = organisms.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
                if (fungusCount > 0) bufferBoost += 5;
            }
            if (organism.energy() > 0 && organism.traits().contains("buffer-stabilizer")) {
                long fungusCount = organisms.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
                if (fungusCount > 0) bufferBoost += 2;
            }
        }
        return new FeedingResult(survivors, totalNutrientContribution, totalMoistureContribution, bufferBoost, deadOrganisms, predatorNutrientContribution);
    }

    private record FeedingBiteResult(int biteSize, List<GardenEvent> events) {}

    private static FeedingBiteResult calculateBite(Organism hunter, Organism prey, Environment environment, int cycle, long rootNetworkCount) {
        List<GardenEvent> events = new ArrayList<>();
        int bite = hunter.type() == OrganismType.FOX ? 3 : 2;
        if (hunter.traits().contains("nutrient-finder")) bite += 1;
        if (hunter.traits().contains("scavenger") && environment.nutrients() < 25) bite += 1;
        if (hunter.traits().contains("nutrient-hoarder")) bite += 1;
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("predator-focus")) bite += 1;
        if (hunter.traits().contains("root-tapper") && rootNetworkCount > 0) bite += 1;
        if (hunter.traits().contains("nutrient-refiner")) {
            if (!hunter.traits().contains("stressed") || hunter.traits().contains("starving")) {
                bite += 1;
                if (hunter.traits().contains("starving")) events.add(new GardenEvent(cycle, "%s refined nutrients while starving.".formatted(hunter.id())));
            }
        }
        if (hunter.traits().contains("gentle-feeder")) bite = Math.max(1, bite - 1);
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

    private static Optional<Integer> findPreyIndex(List<Organism> organisms, Organism hunter, int hunterIndex, int cycle, Environment environment, List<GardenEvent> events) {
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

        if (nutrientScout || resourceTracker) {
            for (int i = 0; i < organisms.size(); i++) {
                if (i == hunterIndex) continue;
                Organism candidate = organisms.get(i);
                if (isValidPrey.test(candidate) && candidate.traits().contains("nutrient-hoarder")) return Optional.of(i);
            }
        }
        
        if (mycelialNetworkScout) {
            long fungusCount = organisms.stream().filter(o -> o.type() == OrganismType.FUNGUS).count();
            if (fungusCount > 0) {
                for (int i = 0; i < organisms.size(); i++) {
                    if (i == hunterIndex) continue;
                    Organism candidate = organisms.get(i);
                    if (isValidPrey.test(candidate)) {
                        events.add(new GardenEvent(cycle, "%s scouted prey using the fungal network.".formatted(hunter.id())));
                        return Optional.of(i);
                    }
                }
            }
        }

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
            if (bestIndex != -1) return Optional.of(bestIndex);
        }

        for (int i = 0; i < organisms.size(); i++) {
            if (i == hunterIndex) continue;
            Organism candidate = organisms.get(i);
            if (isValidPrey.test(candidate)) return Optional.of(i);
        }
        return Optional.empty();
    }

    // --- Population Dynamics Logic ---

    public record PopulationDynamicsContext(
            Environment environment,
            List<Organism> organisms,
            int cycle,
            int nextId,
            List<GardenEvent> events,
            int fungalContribution,
            Random random
    ) {}

    public record PopulationDynamicsResult(List<Organism> organisms, int nextId) {}

    public static PopulationDynamicsResult calculatePopulationDynamics(PopulationDynamicsContext context) {
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

        return new PopulationDynamicsResult(next, identifier);
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
