package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import java.util.Comparator;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.LOCATION_AREAS;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public record ActivateRaidersEvent() implements Event {

    //todo replace record with class so private variables can be used to improve readability (maybe?)

    public static final List<Location> SPACE_AREAS_CLOCKWISE = LOCATION_AREAS.get("Galactica space");

    @Override
    public Followup apply(Game game) {
        val galacticaBoard = game.boards().galactica();
        val raiders = getRaiders(galacticaBoard);
        val basestars = galacticaBoard.shipsInSpace(Basestar.class);

        if (!raiders.isEmpty()) {
            return activateRaidersOneByOne(raiders, galacticaBoard);
        } else if (basestars.isEmpty()) {
            return placeOnCylonFleetBoard();
        } else {
            return launchRaiders(game, basestars);
        }
    }

    private List<Raider> getRaiders(GalacticaBoard galactica) {
        return galactica.shipsInSpace(Raider.class)
                .stream()
                .sorted(Comparator.comparingInt(a -> SPACE_AREAS_CLOCKWISE.indexOf(galactica.locate(a))))
                .toList();
    }

    private Followup activateRaidersOneByOne(List<Raider> raiders, GalacticaBoard galactica) {
        return Followup.all(
                raiders.stream()
                        .map(r -> new ActivateRaiderEvent(r.id()))
                        .toArray(Event[]::new));
    }

    private Followup launchRaiders(Game game, List<Basestar> basestars) {
        val galactica = game.boards().galactica();
        for (val basestar : basestars) {
            val basestarLocation = galactica.locate(basestar);
            for (int i = 0; i < 2; i++) {
                if (game.cylonShips().raiders().isEmpty()) {
                    break;
                }
                galactica.place(basestarLocation, game.cylonShips().raider());
            }
        }
        return Followup.NONE;
    }

    private Followup placeOnCylonFleetBoard() {
        return Followup.all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent());
    }
}
