package xyz.zlatanov.frakkintoasters.state.board;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class BoardTest {

    Board board = new TestBoard(Set.of(COMMAND, GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_6_OCLOCK, BRIG));

    @Test
    void shouldTrackCharacterLocation() {
        board.place(COMMAND, KARA_STARBUCK_THRACE);
        assertEquals(COMMAND, board.locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldRemoveCharacterFromBoard() {
        board.place(COMMAND, GAIUS_BALTAR);
        board.remove(GAIUS_BALTAR);
        assertNull(board.locate(GAIUS_BALTAR));
    }

    @Test
    void shouldRemoveCharacterFromPreviousLocation() {
        board.place(COMMAND, WILLIAM_ADAMA);
        board.place(BRIG, WILLIAM_ADAMA);
        assertTrue(board.charactersIn(COMMAND).isEmpty());
    }

    static class TestBoard extends Board {

        public TestBoard(Set<Location> locations) {
            super(locations);
        }
    }
}