package garden.ai;

import java.util.ArrayList;
import java.util.List;

public class MetabolismCalculator {
    public record MetabolicResult(int metabolism, int energyBonus, List<GardenEvent> events) {}

    public static MetabolicResult calculate(int cycle, Organism organism, Environment environment, int mossContribution, int fungalContribution, int fungalAttractorContribution) {
        int metabolism = organism.type().metabolism();
        int energyBonus = 0;
        List<GardenEvent> events = new ArrayList<>();

        for (String trait : organism.traits()) {
            TraitRegistry.MetabolicEffect effect = TraitRegistry.getMetabolicEffect(trait, cycle, organism, environment, fungalContribution, mossContribution);
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
        return new MetabolicResult(metabolism, energyBonus, events);
    }
}
