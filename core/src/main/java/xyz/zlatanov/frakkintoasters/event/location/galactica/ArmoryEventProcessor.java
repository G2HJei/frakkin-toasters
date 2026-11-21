package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class ArmoryEventProcessor extends LocationActionProcessor<ArmoryEvent> {

    public static boolean canAct(GameView game) {
        return !game.boards().galactica().boardingPartyTrack().isEmpty();
    }

    @Override
    protected boolean isValidAction() {
        return galacticaBoard.boardingPartyTrack().keySet().stream().anyMatch(c -> c.id() == event.centurionId());
    }

    @Override
    protected Followup act() {
        return rollDie(new ArmoryResultEvent(event.centurionId()));
    }
}
