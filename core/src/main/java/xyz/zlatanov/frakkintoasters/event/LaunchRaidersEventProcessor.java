package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_HANGAR_BAY;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

public class LaunchRaidersEventProcessor extends EventProcessor<LaunchRaidersEvent> {

    @Override
    public Followup process() {
        val basestars = galacticaBoard.shipsInSpace(Basestar.class);
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

    private Followup launchRaidersFromEachBasestar(List<Basestar> basestars) {
        basestars.stream()
                .filter(b -> !b.damage().contains(DISABLED_HANGAR_BAY))
                .forEach(this::launch3Raiders);
        return NONE;
    }

    private void launch3Raiders(Basestar basestar) {
        val basestarLocation = galacticaBoard.locate(basestar);
        int raidersLaunched = 0;
        while (raidersLaunched++ < 3) {
            game.cylonShips().raider()
                    .ifPresent(r -> galacticaBoard.place(basestarLocation, r));
        }
    }
}
