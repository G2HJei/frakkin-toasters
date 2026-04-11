package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;

class MakeAdmiralEventTest {
    @Test
    void shouldChangeTheAdmiral() {
        val game = Game.builder().build();
        new MakeAdmiralEvent(SAUL_TIGH).execute(game);
        assertEquals(SAUL_TIGH, game.admiral());
    }
}