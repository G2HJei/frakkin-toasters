package xyz.zlatanov.frakkintoasters.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Location;
import xyz.zlatanov.frakkintoasters.exception.InvalidMoveLocationException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.Character.*;
import static xyz.zlatanov.frakkintoasters.Location.*;

class BoardTest {

    Board board = new TestBoard(Set.of(COMMAND, GALACTICA_SPACE_2_OCLOCK, BRIG));

    @Test
    void shouldTrackCharacterLocation() {
        board.moveTo(COMMAND, KARA_STARBUCK_THRACE);
        assertEquals(COMMAND, board.locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldRemoveCharacterFromBoard() {
        board.moveTo(COMMAND, GAIUS_BALTAR);
        board.remove(GAIUS_BALTAR);
        assertNull(board.locate(GAIUS_BALTAR));
    }

    @Test
    void shouldNotAllowMovingToInvalidLocation() {
        assertThrows(InvalidMoveLocationException.class, () -> board.moveTo(WEAPONS_CONTROL, SAUL_TIGH));
    }

    @Test
    void shouldNotAllowCharactersToMoveToSpaceLocations() {
        assertThrows(InvalidMoveLocationException.class, () -> board.moveTo(GALACTICA_SPACE_2_OCLOCK, LEE_APOLLO_ADAMA));
    }

    @Test
    void shouldNotAllowCharactersToMoveToHazardousLocations() {
        assertThrows(InvalidMoveLocationException.class, () -> board.moveTo(BRIG, TOM_ZAREK));
    }

    static class TestBoard extends Board {

        public TestBoard(Set<Location> locations) {
            super(locations);
        }
    }
}