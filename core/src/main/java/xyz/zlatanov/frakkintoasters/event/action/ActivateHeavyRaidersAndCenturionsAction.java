package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;

import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;

public record ActivateHeavyRaidersAndCenturionsAction() implements Event {

    private static final List<Location> VIPER_LAUNCH_SPACES = List.of(GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK);

    private static final Map<Location, Location> NEXT_STEP_TOWARD_LAUNCH = Map.of(
            GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK,
            GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK,
            GALACTICA_SPACE_8_OCLOCK, GALACTICA_SPACE_6_OCLOCK,
            GALACTICA_SPACE_10_OCLOCK, GALACTICA_SPACE_8_OCLOCK);

    @Override
    public Followup apply(Game game) {
        val galactica = game.boards().galactica();
        galactica.advanceBoardingParty();

        val heavyRaiders = galactica.shipsInSpace(HEAVY_RAIDER).stream()
                .map(HeavyRaider.class::cast)
                .toList();
        val basestars = galactica.shipsInSpace(BASESTAR);
        val anyCenturionsOnMainBoard = !galactica.boardingPartyTrack().isEmpty();

        if (heavyRaiders.isEmpty() && basestars.isEmpty() && !anyCenturionsOnMainBoard) {
            game.boards().cylonFleet().advancePursuit();
            return Followup.NONE;
        }
        if (heavyRaiders.isEmpty()) {
            for (val basestar : basestars) {
                if (game.cylonShips().heavyRaiders().isEmpty()) {
                    break;
                }
                galactica.place(galactica.locate(basestar), game.cylonShips().heavyRaider());
            }
            return Followup.NONE;
        }
        for (val hr : heavyRaiders) {
            val loc = galactica.locate(hr);
            if (VIPER_LAUNCH_SPACES.contains(loc)) {
                if (!game.cylonShips().centurions().isEmpty()) {
                    galactica.remove(hr);
                    game.cylonShips().returned(hr);
                    galactica.boardGalactica(game.cylonShips().centurion());
                }
            } else {
                galactica.remove(hr).place(NEXT_STEP_TOWARD_LAUNCH.get(loc), hr);
            }
        }
        return Followup.NONE;
    }
}
