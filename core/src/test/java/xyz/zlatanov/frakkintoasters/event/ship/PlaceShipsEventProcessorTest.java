package xyz.zlatanov.frakkintoasters.event.ship;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET_SPACE_4;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_9_OCLOCK;

class PlaceShipsEventProcessorTest extends EventTestHarness<PlaceShipsEvent> {

    // "Place a basestar and 3 raiders in front of Galactica and 2 civilian ships behind Galactica."
    @Test
    void shouldPlaceTheShipsInFrontOfAndBehindGalactica() {
        execute(new PlaceShipsEvent(1, 3, 2));

        assertShipCount(GALACTICA_SPACE_9_OCLOCK, Basestar.class, 1);
        assertShipCount(GALACTICA_SPACE_9_OCLOCK, Raider.class, 3);
        assertShipCount(GALACTICA_SPACE_3_OCLOCK, CivilianShip.class, 2);
    }

    // e.g. "Necessary Risk": the Cylon ships go to the chosen space area, the civilian ships still behind Galactica
    @Test
    void shouldPlaceTheCylonShipsInTheChosenArea() {
        execute(new PlaceShipsEvent(1, 3, 1, GALACTICA_SPACE_5_OCLOCK));

        assertShipCount(GALACTICA_SPACE_5_OCLOCK, Basestar.class, 1);
        assertShipCount(GALACTICA_SPACE_5_OCLOCK, Raider.class, 3);
        assertShipCount(GALACTICA_SPACE_3_OCLOCK, CivilianShip.class, 1);
    }

    @Test
    void notInALocationThatIsNoSpaceArea() {
        assertInvalid(new PlaceShipsEvent(1, 0, 0, CYLON_FLEET_SPACE_4));
    }

    @Test
    void shouldPlaceNothingWhenNothingIsAsked() {
        execute(new PlaceShipsEvent(0, 0, 0));

        assertNoShips(GALACTICA_SPACE_9_OCLOCK);
        assertNoShips(GALACTICA_SPACE_3_OCLOCK);
    }

    // Out of basestars in the supply: the one on the Cylon Fleet board (rule 11.4.2).
    @Test
    void shouldTakeTheBasestarFromTheCylonFleetBoardWhenTheSupplyIsOut() {
        basestarAt(CYLON_FLEET_SPACE_4);
        while (cylonShips.basestar().isPresent()) {
            // takes them all out of the supply
        }

        execute(new PlaceShipsEvent(1, 0, 0));

        assertShipCount(GALACTICA_SPACE_9_OCLOCK, Basestar.class, 1);
        assertNoShips(CYLON_FLEET_SPACE_4);
    }

    // None left on any board either (rule 11.4.2).
    @Test
    void shouldPlaceOnlyTheShipsThatAreLeft() {
        while (cylonShips.basestar().isPresent()) {
            // takes them all out of the supply
        }

        execute(new PlaceShipsEvent(1, 0, 0));

        assertNoShips(GALACTICA_SPACE_9_OCLOCK);
    }

    @Test
    void shouldPlaceOnlyTheCivilianShipsThatAreLeft() {
        while (game.decks().civilianShipsLeft() > 1) {
            civilianShips.draw();
        }

        execute(new PlaceShipsEvent(0, 0, 2));

        assertShipCount(GALACTICA_SPACE_3_OCLOCK, CivilianShip.class, 1);
    }
}
