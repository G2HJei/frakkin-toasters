package xyz.zlatanov.frakkintoasters.state.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.ONE_CIVILIAN_SHIP;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.START;

class CylonFleetBoardTest {

    CylonFleetBoard board = new CylonFleetBoard();

    @Test
    void shouldTrackJumpPreparation() {
        board.advancePursuit();
        assertEquals(ONE_CIVILIAN_SHIP, board.pursuitTrack());
        board.advancePursuit();
        board.advancePursuit();
        board.advancePursuit();
        assertEquals(START, board.pursuitTrack());
    }
}