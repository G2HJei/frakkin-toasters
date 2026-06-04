package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;

import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard.VIPER_LAUNCH_SPACES;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.HUMANS_LOSE;

public class ActivateHeavyRaidersAndCenturionsEventProcessor extends EventProcessor<ActivateHeavyRaidersAndCenturionsEvent> {

    private static final Map<Location, Location> NEXT_STEP_TOWARD_LAUNCH = Map.of(
            GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK,
            GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK,
            GALACTICA_SPACE_8_OCLOCK, GALACTICA_SPACE_6_OCLOCK,
            GALACTICA_SPACE_10_OCLOCK, GALACTICA_SPACE_8_OCLOCK);

    @Override
    public Followup process() {
        val heavyRaiders = galacticaBoard.shipsInSpace(HeavyRaider.class);
        val basestars = galacticaBoard.shipsInSpace(Basestar.class);
        val noCenturionsInPlay = galacticaBoard.boardingPartyTrack().isEmpty();

        galacticaBoard.advanceBoardingParty();
        if (galacticaBoard.boardingPartyTrack().containsValue(HUMANS_LOSE)) {
            return one(new CylonsWinEvent());
        }
        if (heavyRaiders.isEmpty() && basestars.isEmpty() && noCenturionsInPlay) {
            return all(
                    new PlaceShipOnCylonFleetBoardEvent(HEAVY_RAIDER),
                    new AdvancePursuitTrackEvent());
        }
        if (heavyRaiders.isEmpty()) {
            return launchHeavyRaiders(basestars);
        }
        for (val hr : heavyRaiders) {
            val loc = galacticaBoard.locate(hr);
            if (VIPER_LAUNCH_SPACES.contains(loc)) {
                boardGalactica(hr);
            } else {
                advanceToEntry(hr, loc);
            }
        }
        return Followup.NONE;
    }

    private Followup launchHeavyRaiders(List<Basestar> basestars) {
        for (val basestar : basestars) {
            val basestarLocation = galacticaBoard.locate(basestar);
            game.cylonShips()
                    .heavyRaider()
                    .ifPresent(hr -> galacticaBoard.place(basestarLocation, hr));
        }
        return Followup.NONE;
    }

    private void boardGalactica(HeavyRaider hr) {
        game.cylonShips()
                .centurion()
                .ifPresent(c -> {
                            galacticaBoard.remove(hr);
                            game.cylonShips().returned(hr);
                            galacticaBoard.boardGalactica(c);
                        }
                );
    }

    private void advanceToEntry(HeavyRaider hr, Location loc) {
        game.moveTo(NEXT_STEP_TOWARD_LAUNCH.get(loc), hr);
    }
}
