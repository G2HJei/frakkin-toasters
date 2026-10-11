package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipOnCylonFleetBoardEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;
import xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView;

import java.util.Comparator;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public class ActivateRaidersEventProcessor extends EventProcessor<ActivateRaidersEvent> {


    @Override
    public Followup process() {
        val raiders = getRaiders(galacticaBoard);
        val basestars = galacticaBoard.shipsInSpace(BasestarView.class);

        if (!raiders.isEmpty()) {
            return activateRaidersOneByOne(raiders, galacticaBoard);
        } else if (basestars.isEmpty()) {
            return placeOnCylonFleetBoard();
        } else {
            return launchRaiders(basestars);
        }
    }

    private List<Raider> getRaiders(GalacticaBoardView galactica) {
        return galactica.shipsInSpace(Raider.class)
                .stream()
                .sorted(Comparator.comparingInt(a -> GALACTICA_SPACE.locations().indexOf(galactica.locate(a))))
                .toList();
    }

    private Followup activateRaidersOneByOne(List<Raider> raiders, GalacticaBoardView galactica) {
        return Followup.all(
                raiders.stream()
                        .map(r -> new ActivateRaiderEvent(r.id()))
                        .toArray(Event[]::new));
    }

    private Followup launchRaiders(List<BasestarView> basestars) {
        basestars.forEach(basestar -> ops.ship(basestar.id()).launchRaiders(2));
        return Followup.NONE;
    }

    private Followup placeOnCylonFleetBoard() {
        return Followup.all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent());
    }
}
