package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.action.title.LaunchNukeEventProcessor.isSpaceArea;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;

public class NecessaryRiskEventProcessor extends MutinyCardEventProcessor<NecessaryRiskEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.NECESSARY_RISK;
    }

    @Override
    protected boolean isValidPlay() {
        return isSpaceArea(event.area());
    }

    // The ops place the Cylon ships that are left, so the card is played even when some can't be.
    @Override
    protected Followup resolve() {
        return all(gain(FOOD, 1), new PlaceShipsEvent(1, 3, 0, event.area()), thenRemove());
    }
}
