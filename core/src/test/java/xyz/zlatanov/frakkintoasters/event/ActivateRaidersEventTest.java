package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

class ActivateRaidersEventTest {

    Game           game           = Game.builder().build();
    GalacticaBoard galacticaBoard = game.boards().galactica();


    @Test
    void shouldPlaceRaiderOnCylonFleetBoardWhenNoRaidersOrBasestarsOnMainBoard() {
        val followup = executeEvent();
        assertEquals(
                all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent()),
                followup);
    }


    @Test
    void shouldLaunchTwoRaidersFromEachBasestarWhenNoRaidersOnBoard() {
        val basestar = game.cylonShips().basestar().orElseThrow();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = executeEvent();

        val raiders = galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Raider.class);
        assertEquals(2, raiders.size());
        assertEquals(NONE, followup);
    }

    @Test
    void shouldLaunchTwoRaidersFromEachOfMultipleBasestars() {
        val basestar1 = game.cylonShips().basestar().orElseThrow();
        val basestar2 = game.cylonShips().basestar().orElseThrow();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar1);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar2);

        executeEvent();

        assertEquals(2, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Raider.class).size());
        assertEquals(2, galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK, Raider.class).size());
    }

    @Test
    void shouldActivateRaidersOneByOne() {
        val raider1 = game.cylonShips().raider().orElseThrow();
        val raider2 = game.cylonShips().raider().orElseThrow();

        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, raider1);
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, raider2);

        val followup = executeEvent();

        assertEquals(all(
                        new ActivateRaiderEvent(raider1.id()),
                        new ActivateRaiderEvent(raider2.id()))
                , followup);
    }

    Followup executeEvent() {
        return new ActivateRaidersEvent().execute(game);
    }
}
