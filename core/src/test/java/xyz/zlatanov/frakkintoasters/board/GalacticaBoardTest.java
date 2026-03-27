package xyz.zlatanov.frakkintoasters.board;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Location;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.Location.*;
import static xyz.zlatanov.frakkintoasters.ship.ShipType.VIPER;
import static xyz.zlatanov.frakkintoasters.ship.ShipType.VIPER_MARK_VII;

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
    void shouldSendCharactersFromDestroyedColonialOneToSickbay() {
        board.move(PRESIDENTS_OFFICE, GAIUS_BALTAR); //hehe
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
    void shouldRemoveFromReserves() {
        board.removeFromReserve(VIPER);
        assertEquals(3, board.reserves().stream().filter(s -> s.type() == VIPER).count());
    }

    @Test
    void shouldRemoveFromDamagedShips() {
        board.removeFromDamagedShips(VIPER_MARK_VII);
        assertEquals(3, board.damagedShips().stream().filter(s -> s.type() == VIPER_MARK_VII).count());
    }

    @Test
    void shouldTrackFighterShips() {
        //val viperToMove = board.reserves()
        //        .stream().findFirst().orElseThrow();
        //board.move(new Viper(), GALACTICA_SPACE_6_OCLOCK);
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