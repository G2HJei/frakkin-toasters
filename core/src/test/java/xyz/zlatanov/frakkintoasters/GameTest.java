package xyz.zlatanov.frakkintoasters;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.ObjectiveCard.EARTH;
import static xyz.zlatanov.frakkintoasters.ObjectiveCard.KOBOL;

class GameTest {

    Game game = new Game();

    @Test
    void shouldSetObjectiveOnlyOnce() {
        game.objective(KOBOL);
        assertThrows(FrakCallTheAdmiralException.class, () -> game.objective(EARTH));
    }

}