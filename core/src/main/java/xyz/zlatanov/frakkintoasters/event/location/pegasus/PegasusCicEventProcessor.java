package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class PegasusCicEventProcessor extends LocationActionProcessor<PegasusCicEvent> {

    public static boolean canAct(GameView game) {
        return !game.boards().galactica().shipsInSpace(BasestarView.class).isEmpty();
    }

    @Override
    protected boolean isValidAction() {
        return isBasestarInSpace(game, event.basestarId());
    }

    @Override
    public Followup process() {
        return rollDie(new PegasusCicResultEvent(event.basestarId()));
    }

    public static boolean isBasestarInSpace(GameView game, int basestarId) {
        return game.boards().galactica().shipsInSpace(BasestarView.class).stream().anyMatch(b -> b.id() == basestarId);
    }
}
