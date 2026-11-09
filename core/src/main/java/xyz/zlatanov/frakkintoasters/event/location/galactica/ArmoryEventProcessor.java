package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

public class ArmoryEventProcessor extends LocationActionProcessor<ArmoryEvent> {

    public static boolean canAct(GameView game) {
        return !game.boards().galactica().boardingPartyTrack().isEmpty();
    }

    @Override
    protected boolean isValidAction() {
        return galacticaBoard.boardingPartyTrack().keySet().stream().anyMatch(c -> c.id() == event.centurionId());
    }

    @Override
    public Followup process() {
        val roll = ops.rollDie();
        if (roll >= 7) {
            ops.galactica().destroyCenturion(event.centurionId());
        }
        return Followup.NONE;
    }
}
