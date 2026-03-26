package xyz.zlatanov.frakkintoasters;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.ObjectiveCard.EARTH;
import static xyz.zlatanov.frakkintoasters.ObjectiveCard.KOBOL;

class GameTest {

    Game game = new Game();

    @Test
    void shouldSetObjectiveOnlyOnce() {
        game.objective(KOBOL);
        assertThrows(FrakCallTheAdmiralException.class, () -> game.objective(EARTH));
    }

    @Test
    void shouldStartWithMandatoryBoards() {
        assertEquals(new GalacticaBoard(), game.galacticaBoard());
    }

}