package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ship.LaunchRaidersEventProcessor;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipOnCylonFleetBoardEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeRaidersActivate;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;
import xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public class ActivateRaidersEventProcessor extends EventProcessor<ActivateRaidersEvent> {


    @Override
    public Followup process() {
        if (game.turn().cylonShipsHeld()) {
            return NONE; // "Negotiation": no Cylon ship launches or activates for the rest of the turn
        }
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

    // The raiders on the board now are the ones that activate, one space area at a time (rule 11.5).
    private Followup activateRaidersOneByOne(List<Raider> raiders, GalacticaBoardView galactica) {
        return Followup.all(
                new OpenInterruptWindowEvent(new BeforeRaidersActivate()), // "Run Interference"
                new ActivateRaiderAreasEvent(raiders.stream().map(Raider::id).toList()));
    }

    private Followup launchRaiders(List<BasestarView> basestars) {
        basestars.stream()
                .filter(LaunchRaidersEventProcessor::canLaunch)
                .forEach(basestar -> ops.ship(basestar.id()).launchRaiders(2));
        return Followup.NONE;
    }

    private Followup placeOnCylonFleetBoard() {
        return Followup.all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent());
    }
}
