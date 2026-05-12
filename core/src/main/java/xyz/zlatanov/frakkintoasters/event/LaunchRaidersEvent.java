package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_HANGAR_BAY;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

public record LaunchRaidersEvent() implements Event {

    @Override
    public Followup apply(Game game) {
        val basestars = game.boards().galactica().shipsInSpace(Basestar.class);
        if (basestars.isEmpty()) {
            return placeBasestarOnCylonFleetBoard();
        } else {
            return launchRaidersFromEachBasestar(game, basestars);
        }
    }

    private Followup placeBasestarOnCylonFleetBoard() {
        return all(new PlaceShipOnCylonFleetBoardEvent(BASESTAR), new AdvancePursuitTrackEvent());
    }

    private Followup launchRaidersFromEachBasestar(Game game, List<Basestar> basestars) {
        basestars.stream()
                .filter(b -> !b.damage().contains(DISABLED_HANGAR_BAY))
                .forEach(b -> launch3Raiders(game, b));
        return NONE;
    }

    private void launch3Raiders(Game game, Basestar basestar) {
        val galacticaBoard = game.boards().galactica();
        val basestarLocation = galacticaBoard.locate(basestar);
        int raidersLaunched = 0;
        while (raidersLaunched++ < 3) {
            game.cylonShips().raider()
                    .ifPresent(r -> galacticaBoard.place(basestarLocation, r));
        }
    }

}
