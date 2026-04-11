package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

class PegasusCicActionEventTest {
    Game game = Game.builder().build();

    @Test
    void shouldDamagePegasus() {
        new PegasusCicActionEvent().execute(game);
        // todo  assertTrue(game.boards().pegasus().damagedLocations().contains(PEGASUS_CIC));
    }
}