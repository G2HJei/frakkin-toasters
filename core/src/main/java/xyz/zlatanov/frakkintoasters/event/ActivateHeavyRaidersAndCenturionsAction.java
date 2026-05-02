package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
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

public record ActivateHeavyRaidersAndCenturionsAction() implements Event {

    private static final Map<Location, Location> NEXT_STEP_TOWARD_LAUNCH = Map.of(
            GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK,
            GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK,
            GALACTICA_SPACE_8_OCLOCK, GALACTICA_SPACE_6_OCLOCK,
            GALACTICA_SPACE_10_OCLOCK, GALACTICA_SPACE_8_OCLOCK);

    @Override
    public Followup apply(Game game) {
        val galactica = game.boards().galactica();
        val heavyRaiders = galactica.shipsInSpace(HeavyRaider.class);
        val basestars = galactica.shipsInSpace(Basestar.class);
        val noCenturionsInPlay = galactica.boardingPartyTrack().isEmpty();

        galactica.advanceBoardingParty();
        if (galactica.boardingPartyTrack().containsValue(HUMANS_LOSE)) {
            return one(new CylonsWinEvent());
        }
        if (heavyRaiders.isEmpty() && basestars.isEmpty() && noCenturionsInPlay) {
            return all(
                    new PlaceShipOnCylonFleetBoardEvent(HEAVY_RAIDER),
                    new AdvancePursuitTrackEvent());
        }
        if (heavyRaiders.isEmpty()) {
            return launchHeavyRaiders(game, basestars);
        }
        for (val hr : heavyRaiders) {
            val loc = galactica.locate(hr);
            if (VIPER_LAUNCH_SPACES.contains(loc)) {
                boardGalactica(game, hr);
            } else {
                advanceToEntry(game, hr, loc);
            }
        }
        return Followup.NONE;
    }

    //todo move in PlaceShipOnCylonFleetBoardEvent
    private static Followup placeOnCylonFleetBoard(Game game) {
        val cylonFleet = game.boards().cylonFleet();
        cylonFleet.advancePursuit();
        val target = CylonFleetBoard.spaceFromRoll(game.die().roll());
        if (!game.cylonShips().heavyRaiders().isEmpty()) {
            cylonFleet.place(target, game.cylonShips().heavyRaider());
        } else {
            spillOutOfShips(game);
        }
        return Followup.NONE;
    }

    //todo move in separate event to follow PlaceShipOnCylonFleetBoardEvent
    private static void spillOutOfShips(Game game) {
        val cylonFleet = game.boards().cylonFleet();
        val galactica = game.boards().galactica();
        for (int i = CylonFleetBoard.SPACE_AREAS.size() - 1; i >= 0; i--) {
            val space = CylonFleetBoard.SPACE_AREAS.get(i);
            val heavyRaiders = cylonFleet.shipsIn(space, HeavyRaider.class);
            if (!heavyRaiders.isEmpty()) {
                val destination = CylonFleetBoard.SPACE_TO_GALACTICA.get(space);
                for (val hr : heavyRaiders) {
                    cylonFleet.remove(hr);
                    galactica.place(destination, hr);
                }
                return;
            }
        }
    }

    private static Followup launchHeavyRaiders(Game game, List<Basestar> basestars) {
        val galactica = game.boards().galactica();
        for (val basestar : basestars) {
            if (game.cylonShips().heavyRaiders().isEmpty()) {
                break;
            }
            val basestarLocation = galactica.locate(basestar);
            val heavyRaider = game.cylonShips().heavyRaider();
            galactica.place(basestarLocation, heavyRaider);
        }
        return Followup.NONE;
    }

    private static void boardGalactica(Game game, HeavyRaider hr) {
        val galactica = game.boards().galactica();
        if (!game.cylonShips().centurions().isEmpty()) {
            galactica.remove(hr);
            game.cylonShips().returned(hr);
            galactica.boardGalactica(game.cylonShips().centurion());
        }
    }

    private static void advanceToEntry(Game game, HeavyRaider hr, Location loc) {
        game.moveTo(NEXT_STEP_TOWARD_LAUNCH.get(loc), hr);
    }
}
