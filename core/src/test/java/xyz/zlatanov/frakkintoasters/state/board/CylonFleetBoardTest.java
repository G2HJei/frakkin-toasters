package xyz.zlatanov.frakkintoasters.state.board;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
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

    // "The Cylon space area numbered "7-8" corresponds to the space area in front of Galactica, the Cylon space areas
    // numbered "4" and "2" correspond to the two space areas on the side of Galactica with the viper launch icons, the
    // Cylon space areas numbered "5-6" and "3" correspond to the two space areas on the side of Galactica without the
    // viper launch icons, and the Cylon space area numbered "1" corresponds to the space area behind Galactica."
    // (rule 11.4)
    @Test
    void shouldMoveEachCylonSpaceAreaToTheMatchingAreaOfTheMainBoard() {
        assertEquals(Map.of(
                CYLON_FLEET_SPACE_7_8, GALACTICA_SPACE_9_OCLOCK, // in front
                CYLON_FLEET_SPACE_4, GALACTICA_SPACE_7_OCLOCK, // the viper launch icon side
                CYLON_FLEET_SPACE_2, GALACTICA_SPACE_5_OCLOCK,
                CYLON_FLEET_SPACE_5_6, GALACTICA_SPACE_11_OCLOCK, // the other side
                CYLON_FLEET_SPACE_3, GALACTICA_SPACE_1_OCLOCK,
                CYLON_FLEET_SPACE_1, GALACTICA_SPACE_3_OCLOCK), // behind
                CylonFleetBoard.MOVE_TO_GALACTICA_MAP);
    }
}