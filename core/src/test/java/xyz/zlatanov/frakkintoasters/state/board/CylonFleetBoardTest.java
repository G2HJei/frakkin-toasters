package xyz.zlatanov.frakkintoasters.state.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.AUTO_ATTACK;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.ONE_CIVILIAN_SHIP;

class CylonFleetBoardTest {

    CylonFleetBoard board = new CylonFleetBoard();

    @Test
    void shouldTrackJumpPreparation() {
        board.advancePursuit();
        assertEquals(ONE_CIVILIAN_SHIP, board.pursuitTrack());
        board.advancePursuit();
        board.advancePursuit();
        board.advancePursuit();
        assertEquals(AUTO_ATTACK, board.pursuitTrack());
        assertThrows(AssertionError.class, () -> board.advancePursuit());
    }
}