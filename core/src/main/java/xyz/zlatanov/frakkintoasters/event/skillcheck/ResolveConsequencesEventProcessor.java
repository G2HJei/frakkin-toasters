package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class ResolveConsequencesEventProcessor extends EventProcessor<ResolveConsequencesEvent> {

    @Override
    public Followup process() {
        return switch (game.activeSkillCheck().type()) {
            case POWER_FAILURE -> single(new DamageGalacticaEvent());
            //todo add the rest consequence events
            default -> NONE;
        };
    }
}
