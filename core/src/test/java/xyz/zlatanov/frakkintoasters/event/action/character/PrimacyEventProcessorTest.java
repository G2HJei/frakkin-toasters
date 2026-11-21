package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.PRIMACY;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

class PrimacyEventProcessorTest extends EventTestHarness<PrimacyEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(CAVIL);
        moveTo(CYLON_FLEET, CAVIL);
    }

    @Test
    void placesABasestarInFrontOfGalactica() {
        execute(new PrimacyEvent(1, null, false));

        assertTrue(game.turn().hasUsed(1, PRIMACY));
        assertFollowup(new PlaceShipsEvent(1, 0, 0, null));
    }

    @Test
    void mayAlsoPlaceRaidersAndACivilianShip() {
        execute(new PrimacyEvent(1, null, true));

        assertFollowup(new PlaceShipsEvent(1, 3, 1, null));
    }

    @Test
    void takesABasestarOffTheBoardWhenAllAreInPlay() {
        val basestars = new ArrayList<Basestar>();
        while (cylonShips.available(BASESTAR) > 0) {
            basestars.add(basestarAt(GALACTICA_SPACE_8_OCLOCK));
        }

        execute(new PrimacyEvent(1, basestars.getFirst().id(), false));

        assertEquals(1, cylonShips.available(BASESTAR));
    }

    @Test
    void cantTakeABasestarOffWhileOneIsLeftToPlace() {
        val placed = basestarAt(GALACTICA_SPACE_8_OCLOCK);

        assertInvalid(new PrimacyEvent(1, placed.id(), false));
    }

    @Test
    void notAnotherCharactersAbility() {
        player(2).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);

        assertInvalid(new PrimacyEvent(2, null, false));
    }

    // With the supply out, the basestar comes from the Cylon Fleet board (rule 11.4.2): PlaceShipsEvent takes it there.
    @Test
    void placesABasestarEvenWithTheSupplyOut() {
        while (cylonShips.available(BASESTAR) > 0) {
            basestarAt(GALACTICA_SPACE_8_OCLOCK);
        }

        execute(new PrimacyEvent(1, null, false));

        assertFollowup(new PlaceShipsEvent(1, 0, 0, null));
    }
}
