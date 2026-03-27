package xyz.zlatanov.frakkintoasters.board;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Location;
import xyz.zlatanov.frakkintoasters.ship.Raptor;
import xyz.zlatanov.frakkintoasters.ship.Viper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.Location.*;
import static xyz.zlatanov.frakkintoasters.ship.ShipType.RAPTOR;
import static xyz.zlatanov.frakkintoasters.ship.ShipType.VIPER;

class GalacticaBoardTest {
    GalacticaBoard board = new GalacticaBoard();

    @Test
    void shouldStartWithAllLocations() {
        assertEquals(startingLocations(), board.locations());
    }

    @Test
    void shouldDestroyColonialOne() {
        board.destroyColonialOne();
        assertEquals(noColonialOneLocations(), board.locations());
        assertTrue(board.colonialOneDestroyed());
    }

    @Test
    void shouldDestroyResurrectionShip() {
        board.destroyResurrectionShip();
        assertFalse(board.locations().contains(RESURRECTION_SHIP));
        assertTrue(board.locations().contains(HUB_DESTROYED));
        assertTrue(board.hubDestroyed());
    }

    @Test
    void shouldSendCharactersFromDestroyedColonialOneToSickbay() {
        board.place(PRESIDENTS_OFFICE, GAIUS_BALTAR); //hehe
        board.destroyColonialOne(); //oops
        assertEquals(SICKBAY, board.locate(GAIUS_BALTAR)); // poor Gaius
    }

    @Test
    void shouldStartWithAllResources() {
        assertEquals(8, board.food());
        assertEquals(10, board.morale());
        assertEquals(12, board.population());
    }

    @Test
    void shouldManageReserves() {
        val viper = new Viper();
        board.addToReserves(viper);
        assertEquals(viper, board.removeFromReserve(VIPER));
    }

    @Test
    void shouldManageDamagedShips() {
        val raptor = new Raptor();
        board.addToDamagedShips(raptor);
        assertEquals(raptor, board.removeFromDamagedShips(RAPTOR));
    }

    @Test
    void shouldPlaceFighterShips() {
        val viper = new Viper();
        board.place(GALACTICA_SPACE_12_OCLOCK, viper);
        assertEquals(List.of(viper), board.shipsIn(GALACTICA_SPACE_12_OCLOCK));
    }

    private Set<Location> startingLocations() {
        return new HashSet<>(List.of(
                FTL_CONTROL,
                WEAPONS_CONTROL,
                COMMUNICATIONS,
                RESEARCH_LAB,
                ARMORY,
                COMMAND,
                ADMIRALS_QUARTERS,
                HANGAR_DECK,
                SICKBAY,
                BRIG,

                PRESS_ROOM,
                PRESIDENTS_OFFICE,
                ADMINISTRATION,

                CAPRICA,
                CYLON_FLEET,
                HUMAN_FLEET,
                RESURRECTION_SHIP,

                GALACTICA_SPACE_12_OCLOCK,
                GALACTICA_SPACE_2_OCLOCK,
                GALACTICA_SPACE_4_OCLOCK,
                GALACTICA_SPACE_6_OCLOCK,
                GALACTICA_SPACE_8_OCLOCK));
    }

    private Set<Location> noColonialOneLocations() {
        val locations = startingLocations();
        locations.removeAll(Set.of(PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION));
        return locations;
    }
}