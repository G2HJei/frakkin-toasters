package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class PlaceCivilianShipEventProcessorTest extends EventTestHarness<PlaceCivilianShipEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldPlaceTheTopCivilianShip() {
        execute(new PlaceCivilianShipEvent(1, GALACTICA_SPACE_8_OCLOCK));
        assertEquals(1, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, CivilianShip.class).size());
    }

    // "The CAG must, if able, place the civilian ship in a space area that does not already have a civilian ship in it."
    @Test
    void shouldNotPlaceNextToAnotherCivilianShipWhileAnAreaIsFree() {
        ops.galactica().placeCivilianShip(GALACTICA_SPACE_8_OCLOCK);
        assertInvalid(new PlaceCivilianShipEvent(1, GALACTICA_SPACE_8_OCLOCK));
    }

    // "If all space areas contain a civilian ship, the CAG may place the new civilian ship in any space area."
    @Test
    void shouldPlaceAnywhereWhenEveryAreaHasACivilianShip() {
        for (Location area : LocationsArea.GALACTICA_SPACE.locations()) {
            ops.galactica().placeCivilianShip(area);
        }

        execute(new PlaceCivilianShipEvent(1, GALACTICA_SPACE_8_OCLOCK));

        assertEquals(2, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, CivilianShip.class).size());
    }

    @Test
    void shouldPlaceInAGalacticaSpaceArea() {
        assertInvalid(new PlaceCivilianShipEvent(1, CYLON_FLEET_SPACE_1));
    }

    @Test
    void shouldNotPlaceWhenNoCivilianShipIsLeft() {
        civilianShips.clear();
        assertInvalid(new PlaceCivilianShipEvent(1, GALACTICA_SPACE_8_OCLOCK));
    }
}
