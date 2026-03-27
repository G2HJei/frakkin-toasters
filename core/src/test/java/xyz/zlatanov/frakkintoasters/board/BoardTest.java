package xyz.zlatanov.frakkintoasters.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Location;
import xyz.zlatanov.frakkintoasters.exception.InvalidMoveLocationException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.Character.*;
import static xyz.zlatanov.frakkintoasters.Location.*;

class BoardTest {

    Board board = new TestBoard(Set.of(COMMAND, GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_6_OCLOCK, BRIG));

    @Test
    void shouldTrackCharacterLocation() {
        board.move(COMMAND, KARA_STARBUCK_THRACE);
        assertEquals(COMMAND, board.locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldRemoveCharacterFromBoard() {
        board.move(COMMAND, GAIUS_BALTAR);
        board.remove(GAIUS_BALTAR);
        assertNull(board.locate(GAIUS_BALTAR));
    }

    @Test
    void shouldRemoveCharacterFromPreviousLocation() {
        board.move(COMMAND, WILLIAM_ADAMA);
        board.move(BRIG, WILLIAM_ADAMA);
        assertTrue(board.charactersIn(COMMAND).isEmpty());
    }

    @Test
    void shouldNotAllowMovingToInvalidLocation() {
        assertThrows(InvalidMoveLocationException.class, () -> board.move(WEAPONS_CONTROL, SAUL_TIGH));
    }

    @Test
    void shouldNotAllowCharactersToMoveSpaceLocations() {
        assertThrows(InvalidMoveLocationException.class, () -> board.move(GALACTICA_SPACE_2_OCLOCK, LEE_APOLLO_ADAMA));
    }


    static class TestBoard extends Board {

        public TestBoard(Set<Location> locations) {
            super(locations);
        }
    }
}