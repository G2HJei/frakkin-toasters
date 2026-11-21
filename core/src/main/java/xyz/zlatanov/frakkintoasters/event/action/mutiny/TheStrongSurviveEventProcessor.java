package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class TheStrongSurviveEventProcessor extends MutinyCardEventProcessor<TheStrongSurviveEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.THE_STRONG_SURVIVE;
    }

    // The card leaves the game before the jump preparation, which may jump the fleet: a whole chain of its own.
    @Override
    protected Followup resolve() {
        return all(new DrawAndDestroyCivilianShipsEvent(1), thenRemove(), new IncreaseJumpPreparationEvent());
    }
}
