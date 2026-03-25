package xyz.zlatanov.frakkintoasters;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.Location.*;

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