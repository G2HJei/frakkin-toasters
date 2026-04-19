package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;

import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard.VIPER_LAUNCH_SPACES;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

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
        if (heavyRaiders.isEmpty() && basestars.isEmpty() && noCenturionsInPlay) {
            return placeOnCylonFleetBoard(game);
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

    private static Followup placeOnCylonFleetBoard(Game game) {
        game.boards().cylonFleet().advancePursuit();
        return Followup.NONE;
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
