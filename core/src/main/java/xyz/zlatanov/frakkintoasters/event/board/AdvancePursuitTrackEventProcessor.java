package xyz.zlatanov.frakkintoasters.event.board;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.placeCivilianShips;

public class AdvancePursuitTrackEventProcessor extends EventProcessor<AdvancePursuitTrackEvent> {
    @Override
    public Followup process() {
        val pursuit = ops.cylonFleet().advancePursuit();
        switch (pursuit) {
            case ONE_CIVILIAN_SHIP:
                return placeCivilianShips(game, 1);
            case TWO_CIVILIAN_SHIPS:
                return placeCivilianShips(game, 2);
            case AUTO_ATTACK:
                transferAllShipsToMainBoard();
            default:
                return Followup.NONE;
        }
    }

    private void transferAllShipsToMainBoard() {
        ops.cylonFleet().resetPursuit();
        ops.cylonFleet().moveShipsToGalactica();
    }
}
