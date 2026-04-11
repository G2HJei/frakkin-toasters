package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;

class Discard1MutinyCardEventTest {

    @Test
    void shouldDiscardSelectedCard() {
        val game = Game.builder().build();
        game.player(1).mutinyCards().add(PANIC);

        new Discard1MutinyCardEvent(1, PANIC).execute(game);

        assertTrue(game.player(1).mutinyCards().isEmpty());
        assertEquals(PANIC, game.player(1).mutinyCards().lastDiscarded());
    }
}