package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import java.util.Comparator;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public class ActivateRaidersEventProcessor extends EventProcessor<ActivateRaidersEvent> {


    @Override
    public Followup process() {
        val galacticaBoard = game.boards().galactica();
        val raiders = getRaiders(galacticaBoard);
        val basestars = galacticaBoard.shipsInSpace(Basestar.class);

        if (!raiders.isEmpty()) {
            return activateRaidersOneByOne(raiders, galacticaBoard);
        } else if (basestars.isEmpty()) {
            return placeOnCylonFleetBoard();
        } else {
            return launchRaiders(basestars);
        }
    }

    private List<Raider> getRaiders(GalacticaBoard galactica) {
        return galactica.shipsInSpace(Raider.class)
                .stream()
                .sorted(Comparator.comparingInt(a -> GALACTICA_SPACE.locations().indexOf(galactica.locate(a))))
                .toList();
    }

    private Followup activateRaidersOneByOne(List<Raider> raiders, GalacticaBoard galactica) {
        return Followup.all(
                raiders.stream()
                        .map(r -> new ActivateRaiderEvent(r.id()))
                        .toArray(Event[]::new));
    }

    private Followup launchRaiders(List<Basestar> basestars) {
        val galactica = game.boards().galactica();
        for (val basestar : basestars) {
            val basestarLocation = galactica.locate(basestar);
            for (int i = 0; i < 2; i++) {
                game.cylonShips().raider()
                        .ifPresent(r -> galactica.place(basestarLocation, r));
            }
        }
        return Followup.NONE;
    }

    private Followup placeOnCylonFleetBoard() {
        return Followup.all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent());
    }
}
