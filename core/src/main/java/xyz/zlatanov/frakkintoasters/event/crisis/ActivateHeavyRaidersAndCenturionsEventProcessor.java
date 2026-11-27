package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ship.LaunchRaidersEventProcessor;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipOnCylonFleetBoardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;

import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView.VIPER_LAUNCH_SPACES;

public class ActivateHeavyRaidersAndCenturionsEventProcessor extends EventProcessor<ActivateHeavyRaidersAndCenturionsEvent> {

    private static final Map<Location, Location> NEXT_STEP_TOWARD_LAUNCH = Map.of(
            GALACTICA_SPACE_1_OCLOCK, GALACTICA_SPACE_3_OCLOCK,
            GALACTICA_SPACE_3_OCLOCK, GALACTICA_SPACE_5_OCLOCK,
            GALACTICA_SPACE_9_OCLOCK, GALACTICA_SPACE_7_OCLOCK,
            GALACTICA_SPACE_11_OCLOCK, GALACTICA_SPACE_9_OCLOCK);

    @Override
    public Followup process() {
        if (game.turn().cylonShipsHeld()) {
            return NONE; // "Negotiation": no Cylon ship launches or activates for the rest of the turn
        }
        val heavyRaiders = galacticaBoard.shipsInSpace(HeavyRaider.class);
        val basestars = galacticaBoard.shipsInSpace(BasestarView.class);
        val noCenturionsInPlay = galacticaBoard.boardingPartyTrack().isEmpty();

        ops.galactica().advanceBoardingParty(); // the engine's WinChecks end the game when a centurion reaches the end
        if (heavyRaiders.isEmpty() && basestars.isEmpty() && noCenturionsInPlay) {
            return Followup.all(
                    new PlaceShipOnCylonFleetBoardEvent(HEAVY_RAIDER),
                    new AdvancePursuitTrackEvent());
        }
        if (heavyRaiders.isEmpty()) {
            return launchHeavyRaiders(basestars);
        }
        for (val hr : heavyRaiders) {
            val loc = galacticaBoard.locate(hr);
            if (VIPER_LAUNCH_SPACES.contains(loc)) {
                ops.ship(hr.id()).boardGalactica();
            } else {
                ops.ship(hr.id()).moveTo(NEXT_STEP_TOWARD_LAUNCH.get(loc));
            }
        }
        return Followup.NONE;
    }

    private Followup launchHeavyRaiders(List<BasestarView> basestars) {
        basestars.stream()
                .filter(LaunchRaidersEventProcessor::canLaunch)
                .forEach(basestar -> ops.ship(basestar.id()).launchHeavyRaider());
        return Followup.NONE;
    }
}
