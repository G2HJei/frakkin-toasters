package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateCylonBasestarsEvent;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public class ResolveConsequencesEventProcessor extends EventProcessor<ResolveConsequencesEvent> {

    @Override
    public Followup process() {
        return switch (game.activeSkillCheck().type()) {
            case DIVISIVE_BEHAVIOR, IN_THE_RING ->
                    single(new PlayerDecisionEvent<>(game.turn().currentPlayerNumber(), SendToSickbayEvent.class));
            case HAUNTED_BY_THE_PAST -> single(new GiveTitlesToNextInLineEvent(game.turn().currentPlayerNumber()));
            case POWER_FAILURE -> single(new DamageGalacticaEvent());
            case THREAT_OF_SUPER_NOVA -> single(new ActivateCylonBasestarsEvent());
            case FIRE_ALL_MISSILES -> all(new DamageGalacticaEvent(), new DamageGalacticaEvent());
            default -> NONE;
        };
    }
}
