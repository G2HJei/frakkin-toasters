package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActivateUnmannedViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.canActivateUnmanned;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;

public class PanicEventProcessor extends MutinyCardEventProcessor<PanicEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.PANIC;
    }

    // "If possible": placing a civilian ship takes no viper away, so the decision still has an answer when it is asked.
    @Override
    protected Followup resolve() {
        val activation = canActivateUnmanned(game)
                ? single(new PlayerDecisionEvent<>(event.playerNumber(), ActivateUnmannedViperEvent.class))
                : NONE;
        return allOf(single(new PlaceShipsEvent(0, 0, 1)), activation, single(thenDiscard()));
    }
}
