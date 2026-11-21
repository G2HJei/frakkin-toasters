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
        if (game.turn().cylonShipsHeld()) {
            return NONE; // "Negotiation": no Cylon ship launches or activates for the rest of the turn
        }
        val basestars = galacticaBoard.shipsInSpace(BasestarView.class);
        if (basestars.isEmpty()) {
            return event.icon() ? placeBasestarOnCylonFleetBoard() : NONE;
        } else {
            return launchRaidersFromEachBasestar(basestars);
        }
    }

    private Followup placeBasestarOnCylonFleetBoard() {
        return all(
                new PlaceShipOnCylonFleetBoardEvent(BASESTAR),
                new AdvancePursuitTrackEvent());
    }

    /**
     * @return whether the basestar can launch raiders or heavy raiders: not with a "Disabled Hangar" token on it (rule
     * 11.7.3), wherever the launch comes from
     */
    public static boolean canLaunch(BasestarView basestar) {
        return !basestar.damage().contains(DISABLED_HANGAR_BAY);
    }

    private Followup launchRaidersFromEachBasestar(List<BasestarView> basestars) {
        basestars.stream()
                .filter(LaunchRaidersEventProcessor::canLaunch)
                .forEach(b -> ops.ship(b.id()).launchRaiders(event.raidersPerBasestar()));
        return NONE;
    }
}
