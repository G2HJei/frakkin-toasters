package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.crisis.OfferUnmannedViperActivationEvent;
import xyz.zlatanov.frakkintoasters.event.character.DutifulEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.canActivateUnmanned;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public class CommandEventProcessor extends LocationActionProcessor<CommandEvent> {

    @Override
    protected Followup act() {
        if (!canActivateUnmanned(game)) {
            return NONE; // the decision would have no answer
        }
        // "up to 2", so the player may stop after each. The second is offered once the first is done: "Alert Viper
        // Pilot" may have boarded the viper it launched (F-78b08a). Then Louis Hoshi's "Dutiful".
        return allOf(activateUnmannedViper(), single(new OfferUnmannedViperActivationEvent(player.number())),
                DutifulEventProcessor.after(game, player, COMMAND));
    }

    private Followup activateUnmannedViper() {
        return one(new PlayerDecisionEvent<>(player.number(), ActivateViperEvent.class), new NoOpEvent(player.number()));
    }
}
