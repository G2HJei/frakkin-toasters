package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipOnCylonFleetBoardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;

import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.HUMANS_LOSE;
import static xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView.VIPER_LAUNCH_SPACES;

public class ActivateHeavyRaidersAndCenturionsEventProcessor extends EventProcessor<ActivateHeavyRaidersAndCenturionsEvent> {

    private static final Map<Location, Location> NEXT_STEP_TOWARD_LAUNCH = Map.of(
            GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK,
            GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK,
            GALACTICA_SPACE_8_OCLOCK, GALACTICA_SPACE_6_OCLOCK,
            GALACTICA_SPACE_10_OCLOCK, GALACTICA_SPACE_8_OCLOCK);

    @Override
    public Followup process() {
        val heavyRaiders = galacticaBoard.shipsInSpace(HeavyRaider.class);
        val basestars = galacticaBoard.shipsInSpace(BasestarView.class);
        val noCenturionsInPlay = galacticaBoard.boardingPartyTrack().isEmpty();

        ops.galactica().advanceBoardingParty();
        if (galacticaBoard.boardingPartyTrack().containsValue(HUMANS_LOSE)) {
            return one(new CylonsWinEvent());
        }
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
        basestars.forEach(basestar -> ops.ship(basestar.id()).launchHeavyRaider());
        return Followup.NONE;
    }
}
