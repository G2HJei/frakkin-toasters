package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;

class MakeAdmiralEventTest {
    @Test
    void shouldChangeTheAdmiral() {
        val game = new Game(KOBOL, 3);
        new MakeAdmiralEvent(SAUL_TIGH).execute(game);
        assertEquals(SAUL_TIGH, game.admiral());
    }
}