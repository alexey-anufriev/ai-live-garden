package garden.ai;

import java.util.ArrayList;
import java.util.List;

public class MetabolismCalculator {
    public record MetabolicResult(int metabolism, int energyBonus, List<GardenEvent> events) {}

    public static MetabolicResult calculate(int cycle, Organism organism, Environment environment, int mossContribution, int fungalContribution, int fungalAttractorContribution) {
        int metabolism = organism.type().metabolism();
        int energyBonus = 0;
        List<GardenEvent> events = new ArrayList<>();

        if (organism.traits().contains("resilient")) {
            metabolism += 1;
        }
        if (organism.traits().contains("dormancy") && environment.nutrients() < 15) {
            metabolism = Math.max(0, metabolism - 2);
        }
        if (organism.traits().contains("metabolic-efficiency")) {
            metabolism = Math.max(0, metabolism - 1);
        }
        if (organism.traits().contains("quiet-hunger") && organism.traits().contains("starving")) {
            metabolism = Math.max(0, metabolism - 1);
        }
        if (organism.traits().contains("nutrient-sharer") && organism.traits().contains("starving")) {
            metabolism = Math.max(0, metabolism - 2);
            events.add(new GardenEvent(cycle, "%s shared metabolic burden while starving.".formatted(organism.id())));
        }
        if (organism.traits().contains("moss-harvester") && mossContribution > 0) {
            metabolism = Math.max(0, metabolism - 1);
            events.add(new GardenEvent(cycle, "%s harvested nutrients from mosses.".formatted(organism.id())));
        }
        if (organism.traits().contains("spore-disperser") && fungalContribution > 0) {
            metabolism = Math.max(0, metabolism - 1);
            events.add(new GardenEvent(cycle, "%s dispersed spores near the fungal network.".formatted(organism.id())));
        }
        if (organism.traits().contains("mycelial-conduit") && fungalContribution > 0) {
            energyBonus += 1;
            events.add(new GardenEvent(cycle, "%s channeled energy through the mycelial network.".formatted(organism.id())));
        }
        if (organism.traits().contains("mycelial-scavenger") && fungalContribution > 0) {
            int reduction = 2;
            if (organism.traits().contains("mycelial-harvester")) {
                reduction += 1;
            }
            metabolism = Math.max(0, metabolism - reduction);
            events.add(new GardenEvent(cycle, "%s scavenged nutrients from the mycelial network%s.".formatted(organism.id(), organism.traits().contains("mycelial-harvester") ? " (harvested)" : "")));
        }
        if (organism.traits().contains("mycelial-resonator") && fungalContribution > 0 && !organism.traits().contains("mycelial-scavenger")) {
            metabolism = Math.max(0, metabolism - 1);
            events.add(new GardenEvent(cycle, "%s resonated with the mycelial network.".formatted(organism.id())));
        }
        if (organism.traits().contains("mycelial-protector") && fungalContribution > 0) {
            metabolism = Math.max(0, metabolism - 2);
            events.add(new GardenEvent(cycle, "%s was protected by the mycelial network.".formatted(organism.id())));
        }
        if (organism.traits().contains("nutrient-anticipator") && environment.nutrientBuffer() < 20) {
            metabolism = Math.max(0, metabolism - 1);
            events.add(new GardenEvent(cycle, "%s anticipated nutrient scarcity.".formatted(organism.id())));
        }
        if (organism.traits().contains("metabolic-economizer") && organism.traits().contains("stressed")) {
            metabolism = Math.max(0, metabolism - 1);
            events.add(new GardenEvent(cycle, "%s economically managed its metabolism while stressed.".formatted(organism.id())));
        }
        if (organism.traits().contains("buffer-scavenger") && environment.nutrientBuffer() > 0) {
            metabolism = Math.max(0, metabolism - 1);
            events.add(new GardenEvent(cycle, "%s utilized the nutrient buffer.".formatted(organism.id())));
        }
        if (organism.traits().contains("buffer-explorer") && environment.nutrientBuffer() > 0) {
            metabolism = Math.max(0, metabolism - 1);
            if (organism.traits().contains("buffer-tapper") && environment.nutrients() < 10) {
                energyBonus += 1;
                events.add(new GardenEvent(cycle, "%s explored and tapped additional buffer nutrients.".formatted(organism.id())));
            } else {
                events.add(new GardenEvent(cycle, "%s explored the nutrient buffer.".formatted(organism.id())));
            }
        }
        if (organism.traits().contains("mycelial-buffer-adapter") && fungalContribution > 0 && environment.nutrientBuffer() > 0) {
            energyBonus += 1;
            metabolism = Math.max(0, metabolism - 1);
            events.add(new GardenEvent(cycle, "%s adapted to buffer tapping through the mycelial network.".formatted(organism.id())));
        }
        if (organism.traits().contains("buffer-tapper") && environment.nutrients() < 10 && environment.nutrientBuffer() > 0) {
            energyBonus += 1;
            events.add(new GardenEvent(cycle, "%s tapped the nutrient buffer while starving.".formatted(organism.id())));
        }
        if (organism.traits().contains("nutrient-scrounger") && environment.nutrients() < 25) {
            energyBonus += 1;
            events.add(new GardenEvent(cycle, "%s scrounged for nutrients.".formatted(organism.id())));
        }
        if (fungalAttractorContribution > 0) {
            energyBonus += 1;
            events.add(new GardenEvent(cycle, "%s was attracted to a fungal-rich area.".formatted(organism.id())));
        }
        return new MetabolicResult(metabolism, energyBonus, events);
    }
}
