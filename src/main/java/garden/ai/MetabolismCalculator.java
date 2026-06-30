package garden.ai;

import java.util.ArrayList;
import java.util.List;

public class MetabolismCalculator {
    public record ContributionContext(int moss, int fungal, int fungalAttractor) {}
    public record MetabolicResult(int metabolism, int energyBonus, List<GardenEvent> events) {}

    public static MetabolicResult calculate(int cycle, Organism organism, Environment environment, ContributionContext contributions) {
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
}
