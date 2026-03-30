package xyz.zlatanov.frakkintoasters;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.EARTH;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;

class GameTest {

    Game game = new Game();

    @Test
    void shouldSetObjectiveOnlyOnce() {
        game.objective(KOBOL);
        assertThrows(FrakCallTheAdmiralException.class, () -> game.objective(EARTH));
    }

}