package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_HANGAR_BAY;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

public class LaunchRaidersEventProcessor extends EventProcessor<LaunchRaidersEvent> {

    @Override
    public Followup process() {
        val basestars = galacticaBoard.shipsInSpace(BasestarView.class);
        if (basestars.isEmpty()) {
            return placeBasestarOnCylonFleetBoard();
        } else {
            return launchRaidersFromEachBasestar(basestars);
        }
    }

    private Followup placeBasestarOnCylonFleetBoard() {
        return all(
                new PlaceShipOnCylonFleetBoardEvent(BASESTAR),
                new AdvancePursuitTrackEvent());
    }

    private Followup launchRaidersFromEachBasestar(List<BasestarView> basestars) {
        basestars.stream()
                .filter(b -> !b.damage().contains(DISABLED_HANGAR_BAY))
                .forEach(b -> ops.ship(b.id()).launchRaiders(3));
        return NONE;
    }
}
