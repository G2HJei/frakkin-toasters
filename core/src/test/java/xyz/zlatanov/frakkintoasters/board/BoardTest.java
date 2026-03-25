package xyz.zlatanov.frakkintoasters.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Location;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.Location.BRIG;

class BoardTest {

    Board board = new TestBoard(Set.of(BRIG));

    @Test
    void shouldTrackCharacterLocation() {
        board.moveTo(BRIG, KARA_STARBUCK_THRACE);
        assertEquals(BRIG, board.locate(KARA_STARBUCK_THRACE));
    }

    static class TestBoard extends Board {

        public TestBoard(Set<Location> locations) {
            super(locations);
        }
    }
}