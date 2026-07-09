package garden.ai;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

public class TraitRegistry {
    private static final Map<String, Integer> NUTRIENT_VALUES = Map.of(
            "nutrient-hoarder", 5,
            "nutrient-recycler", 3,
            "nutrient-decomposer", 7,
            "nutrient-enricher", 5,
            "nutrient-distributor", 4,
            "nutrient-sharer", 3,
            "nutrient-storer", 6
    );

    private static final String[] TRAITS = {"deeper-memory", "brighter-sense", "quiet-hunger", "rain-wise", "shadow-tuned", "resilient", "sun-lover", "sun-seeker", "rain-collector", "nutrient-finder", "nutrient-efficient", "shadow-stepper", "hardy", "water-seeker", "dormancy", "nutrient-weaver", "metabolic-efficiency", "scavenger", "nutrient-sharer", "buffer-resonator", "buffer-scavenger", "nutrient-hoarder", "nutrient-scout", "soil-master", "deep-rooting", "buffer-optimizer", "buffer-tapper", "nutrient-translocator", "camouflaged", "shade-thriver", "moisture-retainer", "nutrient-absorber", "nutrient-synthesizer", "prey-tracker", "resource-tracker", "predator-focus", "nutrient-reclaimer", "nutrient-producer", "nutrient-enricher", "moisture-thriver", "prolific", "cautious-feeder", "nutrient-decomposer", "fungus-soil-enricher", "fungal-network-connector", "fungal-feeder", "mycorrhizal-booster", "nutrient-scrounger", "fungal-symbiote", "nutrient-pump", "nutrient-distributor", "resourceful-breeder", "fungal-enhancer", "mycelial-scavenger", "mycelial-harvester", "mycelial-distributor", "mycelial-resonator", "mycelial-network-scout", "fungal-gardener", "fungal-fertilizer", "nutrient-anticipator", "mycelial-protector", "metabolic-economizer", "spore-disperser", "fungal-root-symbiont", "mycelial-root-mediator", "fungal-attractor", "mycelial-conduit", "mycelial-synergizer", "moss-nutrient-scavenger", "cautious-breeder", "stress-resilient", "stress-avoidance", "buffer-siphon", "fungal-decomposer-mimic", "nutrient-harvester", "fungal-buffer-stabilizer", "fox-specialist", "prolific-spore-producer", "fungal-decomposer-accelerator", "predator-synergy", "spore-dispersal-adaptor", "apex-predator", "beetle-specialist", "nutrient-accelerator", "metabolic-resilience", "root-soil-enricher", "predator-scout", "buffer-reproducer", "predator-converter"};

    public record MetabolicEffect(int metabolismChange, int energyBonus, GardenEvent event) {}

    public record PlantGrowthEffect(int growthChange, GardenEvent event) {}

    public record StressResult(boolean isStressed, int energyLoss, GardenEvent event) {}

    public record ContributionResult(int rootContribution, int fungalContribution, int fungalAttractorContribution, int mossContribution) {}

    public record RootContributionContext(
            long rootNetworkCount,
            long nutrientWeaverCount,
            long nutrientSharerCount,
            long bufferOptimizerCount,
            long soilMasterCount,
            long nutrientRecyclerCount,
            long nutrientTranslocatorCount,
            long nutrientSynthesizerCount,
            long nutrientReclaimerCount,
            long nutrientProducerCount,
            long nutrientPumpCount,
            long nutrientDistributorCount,
            long nutrientAcceleratorCount,
            long rootSoilEnricherCount,
            long fungalRootSymbiontCount,
            long mycelialRootMediatorCount,
            long releaserCount,
            int nutrients,
            int nutrientBuffer
    ) {}

    public record FungalContributionContext(
            long fungusCount,
            long decomposerCount,
            long soilEnricherCount,
            long networkConnectorCount,
            long fungalSymbioteCount,
            long fungalAcceleratorCount,
            long fungalEnhancerCount,
            long fungalBufferStabilizerCount,
            long fungalDecomposerAcceleratorCount,
            long fungalDecompositionEfficiencyCount,
            long fungalNutrientAmplifierCount,
            long fungalGardenerCount,
            long fungalFertilizerCount,
            long rootNetworkCount,
            long mycelialSynergizerCount,
            long fungalDecomposerMimicCount,
            long massDecomposerCount,
            long mossCount,
            long beetleCount,
            int nutrientBuffer
    ) {}

    public static Optional<Integer> findPreyIndex(List<Organism> organisms, Organism hunter, int hunterIndex, int cycle, Environment environment, List<GardenEvent> events) {
        boolean nutrientScout = hunter.traits().contains("nutrient-scout");
        boolean preyTracker = hunter.traits().contains("prey-tracker");
        boolean resourceTracker = hunter.traits().contains("resource-tracker");
        boolean mycelialNetworkScout = hunter.traits().contains("mycelial-network-scout");
        boolean stealthHunter = hunter.traits().contains("stealth-hunter");
        boolean isApexPredator = hunter.traits().contains("apex-predator");
        boolean isPredatorScout = hunter.traits().contains("predator-scout");

        Predicate<Organism> isValidPrey = candidate -> {
            if (candidate.energy() <= 0 || !TraitRegistry.canEat(hunter.type(), candidate.type())) return false;
            
            boolean bypassStealth = stealthHunter || isApexPredator || isPredatorScout ||
                (hunter.traits().contains("coordinated-predator") && 
                 organisms.stream().filter(o -> o.type() == OrganismType.FOX && o.energy() > 0 && !o.id().equals(hunter.id())).count() > 0);

            if (!bypassStealth) {
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

        if (preyTracker || hunter.traits().contains("fox-specialist") || hunter.traits().contains("apex-predator")) {
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

        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("beetle-specialist")) {
            int maxEnergy = -1;
            int bestIndex = -1;
            for (int i = 0; i < organisms.size(); i++) {
                if (i == hunterIndex) continue;
                Organism candidate = organisms.get(i);
                if (isValidPrey.test(candidate) && candidate.type() == OrganismType.BEETLE && candidate.energy() > maxEnergy) {
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

    public static MetabolicEffect calculateMetabolism(int cycle, Organism organism, Environment environment, int mossContribution, int fungalContribution, int fungalAttractorContribution) {
        int metabolism = organism.type().metabolism();
        int energyBonus = 0;
        List<GardenEvent> events = new ArrayList<>();

        for (String trait : organism.traits()) {
            MetabolicEffect effect = getMetabolicEffect(trait, cycle, organism, environment, fungalContribution, mossContribution);
            if (effect != null) {
                metabolism = Math.max(0, metabolism + effect.metabolismChange());
                energyBonus += effect.energyBonus();
                if (effect.event() != null) {
                    events.add(effect.event());
                }
            }
        }

        if (fungalAttractorContribution > 0) {
            energyBonus += 1;
            events.add(new GardenEvent(cycle, "%s was attracted to a fungal-rich area.".formatted(organism.id())));
        }
        
        GardenEvent combinedEvent = events.isEmpty() ? null : new GardenEvent(cycle, events.stream().map(GardenEvent::description).collect(java.util.stream.Collectors.joining("; ")));
        return new MetabolicEffect(metabolism, energyBonus, combinedEvent);
    }

    public static StressResult calculatePlantStress(Organism organism, Environment environment, int cycle, List<Organism> allOrganisms) {
        if (!isPlantStressed(organism, environment, allOrganisms)) {
            return new StressResult(false, 0, null);
        }

        int energyLoss = 0;
        GardenEvent event = null;

        if (environment.nutrients() == 0) {
            energyLoss = 1;
            event = new GardenEvent(cycle, "%s lost energy due to environmental stress.".formatted(organism.id()));
        } else if (allOrganisms.stream().filter(o -> o.type().isPlant()).count() > 5000) {
            energyLoss = 1;
            event = new GardenEvent(cycle, "%s lost energy due to overcrowding.".formatted(organism.id()));
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
        boolean isMetabolicResilient = organism.traits().contains("metabolic-resilience");
        boolean overcrowded = allOrganisms.stream().filter(o -> o.type().isAnimal()).count() > 2500;
        return ((environment.nutrients() + environment.nutrientBuffer() / 2) < 25 || overcrowded) && !isResilient && !isDormant && !isMetabolicResilient;
    }

    public static ContributionResult calculateContribution(List<Organism> organisms, Environment environment) {
        long releaserCount = TraitRegistry.count(organisms, "buffer-releaser");
        int rootContribution = calculateRootContribution(organisms, environment, releaserCount);
        int fungalContribution = calculateFungalContribution(organisms, environment);
        int fungalAttractorContribution = (TraitRegistry.count(organisms, "fungal-attractor", OrganismType.ROOT_NETWORK) > 0 && fungalContribution > 0) ? 1 : 0;
        int mossContribution = organisms.stream().anyMatch(organism -> organism.type() == OrganismType.MOSS) ? 1 : 0;
        return new ContributionResult(rootContribution, fungalContribution, fungalAttractorContribution, mossContribution);
    }

    public static int calculateRootContribution(List<Organism> organisms, Environment environment, long releaserCount) {
        long fungusCount = TraitRegistry.count(organisms, OrganismType.FUNGUS);
        long mycelialRootMediatorCount = (fungusCount > 0) ? TraitRegistry.countAnimalTrait(organisms, "mycelial-root-mediator") : 0;

        var context = new RootContributionContext(
                TraitRegistry.count(organisms, OrganismType.ROOT_NETWORK),
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
                TraitRegistry.count(organisms, "nutrient-accelerator", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "root-soil-enricher", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "fungal-root-symbiont", OrganismType.ROOT_NETWORK),
                mycelialRootMediatorCount,
                releaserCount,
                environment.nutrients(),
                environment.nutrientBuffer()
        );
        return calculateRoot(context);
    }

    private static int calculateRoot(RootContributionContext context) {
        if (context.nutrients() < 5) {
            return (int) (context.rootNetworkCount() * 10 + context.nutrientWeaverCount() * 10 + context.nutrientSharerCount() * 20 + context.bufferOptimizerCount() * 20 + context.soilMasterCount() * 30 + context.nutrientRecyclerCount() * 10 + context.nutrientTranslocatorCount() * 40 + context.nutrientSynthesizerCount() * 30 + context.nutrientReclaimerCount() * 25 + context.nutrientProducerCount() * 50 + context.nutrientPumpCount() * 60 + context.nutrientDistributorCount() * 40 + context.nutrientAcceleratorCount() * 50 + context.rootSoilEnricherCount() * 70 + context.fungalRootSymbiontCount() * 40 + context.mycelialRootMediatorCount() * 20);
        } else if (context.nutrients() < 10) {
            return (int) (context.rootNetworkCount() * 8 + context.nutrientWeaverCount() * 8 + context.nutrientSharerCount() * 16 + context.bufferOptimizerCount() * 16 + context.soilMasterCount() * 24 + context.nutrientRecyclerCount() * 8 + context.nutrientTranslocatorCount() * 32 + context.nutrientSynthesizerCount() * 24 + context.nutrientReclaimerCount() * 20 + context.nutrientProducerCount() * 40 + context.nutrientPumpCount() * 48 + context.nutrientDistributorCount() * 32 + context.nutrientAcceleratorCount() * 40 + context.rootSoilEnricherCount() * 56 + context.fungalRootSymbiontCount() * 32 + context.mycelialRootMediatorCount() * 16);
        } else if (context.nutrients() < 25) {
            return (int) (context.rootNetworkCount() * 4 + context.nutrientWeaverCount() * 4 + context.nutrientSharerCount() * 8 + context.bufferOptimizerCount() * 8 + context.soilMasterCount() * 12 + context.nutrientRecyclerCount() * 4 + context.nutrientTranslocatorCount() * 16 + context.nutrientSynthesizerCount() * 12 + context.nutrientReclaimerCount() * 10 + context.nutrientProducerCount() * 20 + context.nutrientPumpCount() * 24 + context.nutrientDistributorCount() * 16 + context.nutrientAcceleratorCount() * 20 + context.rootSoilEnricherCount() * 28 + context.fungalRootSymbiontCount() * 16 + context.mycelialRootMediatorCount() * 8);
        } else {
            int bufferBonus = (context.nutrientBuffer() > 50) ? 2 : 0;
            int recyclerBonus = (context.nutrientBuffer() > 50 ? 5 : 2) + (int) Math.min(context.releaserCount(), 10);
            return (int) (Math.max(1, context.rootNetworkCount() / 2) + context.nutrientWeaverCount() + context.nutrientSharerCount() * 2 + context.bufferOptimizerCount() * (2 + bufferBonus) + context.soilMasterCount() * 4 + context.nutrientRecyclerCount() * recyclerBonus + context.nutrientTranslocatorCount() * 4 + context.nutrientSynthesizerCount() * 3 + context.nutrientReclaimerCount() * 3 + context.nutrientProducerCount() * 5 + context.nutrientPumpCount() * 6 + context.nutrientDistributorCount() * 4 + context.nutrientAcceleratorCount() * 5 + context.rootSoilEnricherCount() * (10 + bufferBonus) + context.fungalRootSymbiontCount() * 4 + context.mycelialRootMediatorCount() * 2);
        }
    }

    public static int calculateFungalContribution(List<Organism> organisms, Environment environment) {
        var context = new FungalContributionContext(
                TraitRegistry.count(organisms, OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "nutrient-decomposer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungus-soil-enricher", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-network-connector", OrganismType.FUNGUS),
                TraitRegistry.countPlantTrait(organisms, "fungal-symbiote"),
                TraitRegistry.count(organisms, "fungal-accelerator", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-enhancer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-buffer-stabilizer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-decomposer-accelerator", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-decomposition-efficiency", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, "fungal-nutrient-amplifier", OrganismType.FUNGUS),
                TraitRegistry.countAnimalTrait(organisms, "fungal-gardener"),
                TraitRegistry.countAnimalTrait(organisms, "fungal-fertilizer"),
                TraitRegistry.count(organisms, OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "mycelial-synergizer", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "fungal-decomposer-mimic", OrganismType.ROOT_NETWORK),
                TraitRegistry.count(organisms, "mass-decomposer", OrganismType.FUNGUS),
                TraitRegistry.count(organisms, OrganismType.MOSS),
                TraitRegistry.count(organisms, OrganismType.BEETLE),
                environment.nutrientBuffer()
        );
        return calculateFungal(context);
    }

    private static int calculateFungal(FungalContributionContext context) {
        int connectorBonus = (context.rootNetworkCount() > 0) ? 6 : 4;
        int synergizerBonus = (context.mycelialSynergizerCount() > 0 && context.fungusCount() > 0) ? 5 : 0;
        int bufferBonus = (context.nutrientBuffer() > 20) ? 2 : 1;
        int decayPressure = (int) Math.min((context.mossCount() + context.beetleCount()) / 100, 10);

        return (int) (context.fungusCount() * 2 * bufferBonus +
                      context.decomposerCount() * (30 + decayPressure) * bufferBonus +
                      context.soilEnricherCount() * 10 * bufferBonus +
                      context.networkConnectorCount() * connectorBonus * bufferBonus +
                      context.fungalSymbioteCount() * 2 +
                      context.fungalAcceleratorCount() * 15 +
                      context.fungalEnhancerCount() * 8 +
                      context.fungalBufferStabilizerCount() * 12 +
                      context.fungalDecomposerAcceleratorCount() * 45 +
                      context.fungalDecompositionEfficiencyCount() * 120 +
                      context.fungalNutrientAmplifierCount() * 30 +
                      context.fungalGardenerCount() * 5 +
                      context.fungalFertilizerCount() * 7 +
                      context.fungalDecomposerMimicCount() * 5 +
                      context.massDecomposerCount() * decayPressure * 10) + synergizerBonus;
    }

    public record BiteEffect(int biteSize, List<GardenEvent> events) {}

    public static BiteEffect calculateBite(Organism hunter, Organism prey, Environment environment, int cycle, long rootNetworkCount, List<Organism> allOrganisms) {
        List<GardenEvent> events = new ArrayList<>();
        int bite = hunter.type() == OrganismType.FOX ? 3 : 2;
        if (hunter.traits().contains("nutrient-finder")) bite += 1;
        if (hunter.traits().contains("scavenger") && environment.nutrients() < 25) bite += 1;
        if (hunter.traits().contains("nutrient-hoarder")) bite += 1;
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("predator-focus")) bite += 1;
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("predator-scout")) bite += 1;
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("fox-specialist")) bite += 2;
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("apex-predator")) bite += 3;
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("predator-converter")) {
            bite += 4;
            events.add(new GardenEvent(cycle, "%s utilized predator-converter to increase energy gain.".formatted(hunter.id())));
        }
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("predator-energy-efficiency")) {
            bite += 6;
            events.add(new GardenEvent(cycle, "%s optimized energy efficiency using predator-energy-efficiency.".formatted(hunter.id())));
        }
        if (hunter.type() == OrganismType.FOX && hunter.traits().contains("beetle-specialist") && prey.type() == OrganismType.BEETLE) {
            bite += 2;
            events.add(new GardenEvent(cycle, "%s specialized in beetle hunting.".formatted(hunter.id())));
        }
        if (hunter.traits().contains("predator-synergy") && hunter.type() == OrganismType.FOX) {
            long foxCount = allOrganisms.stream().filter(o -> o.type() == OrganismType.FOX && o.energy() > 0 && !o.id().equals(hunter.id())).count();
            int bonus = (int) Math.min(foxCount, 3);
            bite += bonus;
            if (bonus > 0) events.add(new GardenEvent(cycle, "%s gained +%d synergy from nearby foxes.".formatted(hunter.id(), bonus)));
        }
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
        return new BiteEffect(bite, events);
    }

    public static int getNutrientValueModifier(String trait) {
        return NUTRIENT_VALUES.getOrDefault(trait, 0);
    }

    public static int getReproductionThresholdModifier(String trait, Environment environment, int fungalContribution, Organism organism) {
        int modifier = 0;
        switch (trait) {
            case "prolific":
                modifier -= 3;
                break;
            case "resourceful-breeder":
                if (environment.nutrients() < 25) modifier -= 8;
                break;
            case "fungal-nurturer":
                if (fungalContribution > 0) modifier -= 3;
                break;
            case "fox-specialist":
                modifier -= 2;
                break;
            case "apex-predator":
                modifier -= 3;
                break;
            case "buffer-reproducer":
                if (environment.nutrientBuffer() > 40) modifier -= 5;
                break;
            case "fungal-decomposer-accelerator":
                if (organism.type() == OrganismType.FUNGUS) modifier -= 4;
                break;
            case "fungal-nutrient-amplifier":
                if (organism.type() == OrganismType.FUNGUS) modifier -= 3;
                break;
            case "fungal-decomposition-efficiency":
                if (organism.type() == OrganismType.FUNGUS) modifier -= 8;
                break;
            case "fungal-root-symbiont":
                if (organism.type() == OrganismType.ROOT_NETWORK && fungalContribution > 0) modifier -= 3;
                break;
            case "root-soil-enricher":
                if (organism.type() == OrganismType.ROOT_NETWORK && environment.nutrientBuffer() > 40) modifier -= 6;
                break;
            case "nutrient-dependent-reproduction":
                if (environment.nutrients() > 75) modifier -= 8;
                else if (environment.nutrients() > 50) modifier -= 4;
                break;
            case "reproductive-efficiency":
                if (organism.type() == OrganismType.FOX || organism.type() == OrganismType.FUNGUS || organism.type() == OrganismType.ROOT_NETWORK) {
                    modifier -= 3;
                }
                break;
        }
        return modifier;
    }

    public static String getMutationTrait(int cycle, Organism organism, OrganismType childType, Environment environment) {
        if (childType == OrganismType.FUNGUS && Math.random() < 0.5) {
            String[] fungalTraits = {"nutrient-decomposer", "fungus-soil-enricher", "fungal-network-connector", "fungal-accelerator", "fungal-enhancer", "fungal-buffer-stabilizer", "nutrient-synthesizer", "buffer-tapper", "fungal-nutrient-amplifier", "mass-decomposer", "reproductive-efficiency", "fungal-decomposition-efficiency", "nutrient-dependent-reproduction"};
            return fungalTraits[Math.floorMod(cycle + organism.generation(), fungalTraits.length)];
        }
        if (childType == OrganismType.FOX && Math.random() < 0.3) {
            return (Math.random() < 0.5) ? "reproductive-efficiency" : "nutrient-dependent-reproduction";
        }
        if (childType == OrganismType.ROOT_NETWORK && Math.random() < 0.3) {
            return (Math.random() < 0.5) ? "reproductive-efficiency" : "nutrient-dependent-reproduction";
        }
        if (environment.nutrients() < 40 && Math.random() < 0.3) {
            if (childType.isPlant()) return "dormancy";
            if (childType.isAnimal()) return "metabolic-efficiency";
        }
        int index = Math.floorMod(organism.id().hashCode() + cycle + organism.generation(), TRAITS.length);
        return TRAITS[index];
    }

    public static PlantGrowthEffect getPlantGrowthEffect(String trait, int cycle, Organism organism, Environment environment, List<Organism> organisms, int fungalContribution) {
        switch (trait) {
            case "water-seeker":
                if (environment.moisture() < 50) return new PlantGrowthEffect(1, null);
                break;
            case "sun-seeker":
                if (environment.light() > 60) return new PlantGrowthEffect(1, new GardenEvent(cycle, "%s thrived in the sunlight.".formatted(organism.id())));
                break;
            case "sun-lover":
                if (environment.light() > 60) return new PlantGrowthEffect(1, null);
                break;
            case "shade-thriver":
                if (environment.light() < 40) return new PlantGrowthEffect(2, new GardenEvent(cycle, "%s thrived in the shade.".formatted(organism.id())));
                break;
            case "rain-collector":
                if (environment.moisture() < 40) return new PlantGrowthEffect(1, null);
                break;
            case "hardy":
                if (organism.type() == OrganismType.FERN && environment.warmth() > 50) return new PlantGrowthEffect(1, null);
                break;
            case "nutrient-efficient":
                if (environment.nutrients() < 30) return new PlantGrowthEffect(1, null);
                break;
            case "nutrient-synthesizer":
                if (environment.nutrients() < 5) return new PlantGrowthEffect(2, new GardenEvent(cycle, "%s synthesized nutrients from the soil.".formatted(organism.id())));
                break;
            case "buffer-tapper":
                if (environment.nutrients() < 5 && environment.nutrientBuffer() > 0) return new PlantGrowthEffect(2, new GardenEvent(cycle, "%s tapped the nutrient buffer.".formatted(organism.id())));
                break;
            case "buffer-resonator":
                if (environment.nutrientBuffer() > 0) return new PlantGrowthEffect(1, new GardenEvent(cycle, "%s utilized the nutrient buffer.".formatted(organism.id())));
                break;
            case "moisture-thriver":
                if (environment.moisture() > 60) return new PlantGrowthEffect(2, new GardenEvent(cycle, "%s thrived in the moisture.".formatted(organism.id())));
                break;
            case "fungal-feeder":
                if (fungalContribution > 0) {
                    boolean hasMycorrhizalBooster = organisms.stream().anyMatch(o -> o.type() == OrganismType.FUNGUS && o.traits().contains("mycorrhizal-booster"));
                    int bonus = hasMycorrhizalBooster ? 2 : 1;
                    return new PlantGrowthEffect(bonus, new GardenEvent(cycle, "%s fed on fungal networks%s.".formatted(organism.id(), hasMycorrhizalBooster ? " (boosted)" : "")));
                }
                break;
            case "resilient":
                return new PlantGrowthEffect(-1, null);
            case "metabolic-resilience":
                if (environment.nutrients() < 25) return new PlantGrowthEffect(1, new GardenEvent(cycle, "%s resiliently grew despite scarcity.".formatted(organism.id())));
                break;
            case "root-soil-enricher":
                if (environment.nutrientBuffer() > 50) return new PlantGrowthEffect(2, new GardenEvent(cycle, "%s enriched the soil using the nutrient buffer.".formatted(organism.id())));
                break;
        }
        return null;
    }

    public static MetabolicEffect getMetabolicEffect(String trait, int cycle, Organism organism, Environment environment, int fungalContribution, int mossContribution) {
        switch (trait) {
            case "resilient":
                return new MetabolicEffect(1, 0, null);
            case "metabolic-resilience":
                return new MetabolicEffect(-2, 1, new GardenEvent(cycle, "%s thrived by managing its metabolism.".formatted(organism.id())));
            case "dormancy":
                if (environment.nutrients() < 15) return new MetabolicEffect(-2, 0, null);
                break;
            case "metabolic-efficiency":
                return new MetabolicEffect(-1, 0, null);
            case "quiet-hunger":
                if (organism.traits().contains("starving")) return new MetabolicEffect(-1, 0, null);
                break;
            case "nutrient-sharer":
                if (organism.traits().contains("starving")) return new MetabolicEffect(-2, 0, new GardenEvent(cycle, "%s shared metabolic burden while starving.".formatted(organism.id())));
                break;
            case "moss-harvester":
                if (mossContribution > 0) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s harvested nutrients from mosses.".formatted(organism.id())));
                break;
            case "spore-disperser":
                if (fungalContribution > 0) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s dispersed spores near the fungal network.".formatted(organism.id())));
                break;
            case "mycelial-conduit":
                if (fungalContribution > 0) return new MetabolicEffect(0, 1, new GardenEvent(cycle, "%s channeled energy through the mycelial network.".formatted(organism.id())));
                break;
            case "mycelial-scavenger":
                if (fungalContribution > 0) {
                    int reduction = 2;
                    if (organism.traits().contains("mycelial-harvester")) {
                        reduction += 1;
                    }
                    return new MetabolicEffect(-reduction, 0, new GardenEvent(cycle, "%s scavenged nutrients from the mycelial network%s.".formatted(organism.id(), organism.traits().contains("mycelial-harvester") ? " (harvested)" : "")));
                }
                break;
            case "mycelial-resonator":
                if (fungalContribution > 0 && !organism.traits().contains("mycelial-scavenger")) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s resonated with the mycelial network.".formatted(organism.id())));
                break;
            case "mycelial-protector":
                if (fungalContribution > 0) return new MetabolicEffect(-2, 0, new GardenEvent(cycle, "%s was protected by the mycelial network.".formatted(organism.id())));
                break;
            case "nutrient-anticipator":
                if (environment.nutrientBuffer() < 20) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s anticipated nutrient scarcity.".formatted(organism.id())));
                break;
            case "metabolic-economizer":
                if (organism.traits().contains("stressed")) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s economically managed its metabolism while stressed.".formatted(organism.id())));
                break;
            case "buffer-scavenger":
                if (environment.nutrientBuffer() > 0) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s utilized the nutrient buffer.".formatted(organism.id())));
                break;
            case "buffer-explorer":
                if (environment.nutrientBuffer() > 0) {
                    if (organism.traits().contains("buffer-tapper") && environment.nutrients() < 10) return new MetabolicEffect(-1, 1, new GardenEvent(cycle, "%s explored and tapped additional buffer nutrients.".formatted(organism.id())));
                    else return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s explored the nutrient buffer.".formatted(organism.id())));
                }
                break;
            case "mycelial-buffer-adapter":
                if (fungalContribution > 0 && environment.nutrientBuffer() > 0) return new MetabolicEffect(-1, 1, new GardenEvent(cycle, "%s adapted to buffer tapping through the mycelial network.".formatted(organism.id())));
                break;
            case "buffer-tapper":
                if (environment.nutrients() < 10 && environment.nutrientBuffer() > 0) return new MetabolicEffect(0, 1, new GardenEvent(cycle, "%s tapped the nutrient buffer while starving.".formatted(organism.id())));
                break;
            case "nutrient-scrounger":
                if (environment.nutrients() < 25) return new MetabolicEffect(0, 1, new GardenEvent(cycle, "%s scrounged for nutrients.".formatted(organism.id())));
                break;
            case "fungal-decomposition-efficiency":
                if (organism.type() == OrganismType.FUNGUS) return new MetabolicEffect(0, 8, new GardenEvent(cycle, "%s optimized its decomposition to gain energy.".formatted(organism.id())));
                break;
            case "fungal-decomposer-accelerator":
                if (organism.type() == OrganismType.FUNGUS) return new MetabolicEffect(0, 2, new GardenEvent(cycle, "%s accelerated decomposition to gain energy.".formatted(organism.id())));
                break;
            case "fungal-nutrient-amplifier":
                if (organism.type() == OrganismType.FUNGUS) return new MetabolicEffect(0, 1, new GardenEvent(cycle, "%s amplified nutrient intake.".formatted(organism.id())));
                break;
            case "mass-decomposer":
                if (organism.type() == OrganismType.FUNGUS) return new MetabolicEffect(0, 3, new GardenEvent(cycle, "%s decomposed mass to gain energy.".formatted(organism.id())));
                break;
            case "root-soil-enricher":
                if (environment.nutrientBuffer() > 50) return new MetabolicEffect(0, 1, new GardenEvent(cycle, "%s enriched the soil and gained energy.".formatted(organism.id())));
                break;
            case "predator-scout":
                if (environment.nutrients() < 25) return new MetabolicEffect(-1, 0, new GardenEvent(cycle, "%s scouted for prey in scarce conditions.".formatted(organism.id())));
                break;
        }
        return null;
    }

    public static boolean canEat(OrganismType eater, OrganismType eaten) {
        return eater.prey().contains(eaten);
    }

    public static OrganismType offspringType(OrganismType type, int cycle, int generation, Environment environment) {
        return type.offspringType(cycle, generation, environment);
    }

    public static OrganismType offspringType(Organism organism, int cycle, int generation, Environment environment) {
        return organism.type().offspringType(cycle, generation, environment, organism.traits());
    }

    public static long count(List<Organism> organisms, String trait) {
        return organisms.stream().filter(o -> o.traits().contains(trait)).count();
    }

    public static long count(List<Organism> organisms, OrganismType type) {
        return organisms.stream().filter(o -> o.type() == type).count();
    }

    public static long count(List<Organism> organisms, String trait, OrganismType type) {
        return organisms.stream().filter(o -> o.type() == type && o.traits().contains(trait)).count();
    }

    public static long countAnimalTrait(List<Organism> organisms, String trait) {
        return organisms.stream().filter(o -> o.type().isAnimal() && o.traits().contains(trait)).count();
    }

    public static long countPlantTrait(List<Organism> organisms, String trait) {
        return organisms.stream().filter(o -> o.type().isPlant() && o.traits().contains(trait)).count();
    }
}
