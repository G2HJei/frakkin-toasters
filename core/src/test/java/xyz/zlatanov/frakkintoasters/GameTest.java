package xyz.zlatanov.frakkintoasters;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;

class GameTest {


    @Test
    void shouldCreateGame() {
        val game = new Game(KOBOL, 2);
        assertEquals(KOBOL, game.objective());
        assertEquals(2, game.players().size());
    }

}