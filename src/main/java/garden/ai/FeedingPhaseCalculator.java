package garden.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Calculator for the feeding phase of the garden simulation.
 */
public class FeedingPhaseCalculator {

    public record FeedingResult(List<Organism> organisms, int totalNutrientContribution, int totalMoistureContribution, int nutrientBufferBoost, int culledPlantCount, int predatorNutrientContribution) {
    }

    public static FeedingResult calculate(List<Organism> organisms, Environment environment, int cycle, List<GardenEvent> events) {
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
                if (isValidPrey.test(candidate) && candidate.traits().contains("nutrient-hoarder")) {
                    return Optional.of(i);
                }
            }
        }
        
        if (mycelialNetworkScout) {
            // Need a way to check fungal contribution without passing it directly, maybe through traits?
            // Re-using the check from Garden.java seems tricky, but we have the organisms list.
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
            if (bestIndex != -1) {
                return Optional.of(bestIndex);
            }
        }

        for (int i = 0; i < organisms.size(); i++) {
            if (i == hunterIndex) continue;
            Organism candidate = organisms.get(i);
            if (isValidPrey.test(candidate)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }
}
