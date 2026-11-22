package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.optional;

public class OfferUnmannedViperActivationEventProcessor extends EventProcessor<OfferUnmannedViperActivationEvent> {

    @Override
    public Followup process() {
        // A pilot activates his own viper, so a CAG who is piloting has none to activate here.
        val piloting = player.character() != null && galacticaBoard.pilotedFighter(player.character()).isPresent();
        return piloting || !ActivateViperEventProcessor.canActivateUnmanned(game) ? NONE
                : optional(player.number(), new PlayerDecisionEvent<>(player.number(), ActivateViperEvent.class));
    }
}
