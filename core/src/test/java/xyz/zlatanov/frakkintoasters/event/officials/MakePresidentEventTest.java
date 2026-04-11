package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;

class MakePresidentEventTest {

    @Test
    void shouldChangeThePresident() {
        val game = Game.builder().build();
        new MakePresidentEvent(GAIUS_BALTAR).execute(game);
        assertEquals(GAIUS_BALTAR, game.president());
    }

}