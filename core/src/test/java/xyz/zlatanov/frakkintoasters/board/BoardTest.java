package xyz.zlatanov.frakkintoasters.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Location;
import xyz.zlatanov.frakkintoasters.exception.InvalidMoveLocationException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.Character.*;
import static xyz.zlatanov.frakkintoasters.Location.*;

class BoardTest {

    Board board = new TestBoard(Set.of(BRIG));

    @Test
    void shouldTrackCharacterLocation() {
        board.moveTo(BRIG, KARA_STARBUCK_THRACE);
        assertEquals(BRIG, board.locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldRemoveCharacterFromBoard() {
        board.moveTo(BRIG, GAIUS_BALTAR);
        board.remove(GAIUS_BALTAR);
        assertNull(board.locate(GAIUS_BALTAR));
    }

    @Test
    void shouldNotAllowMovingToInvalidLocation() {
        assertThrows(InvalidMoveLocationException.class, () -> board.moveTo(WEAPONS_CONTROL, SAUL_TIGH));
    }

    @Test
    void shouldAllowOnlyPilotsToMoveToSpaceLocations() {
        //todo
    }

    static class TestBoard extends Board {

        public TestBoard(Set<Location> locations) {
            super(locations);
        }
    }
}