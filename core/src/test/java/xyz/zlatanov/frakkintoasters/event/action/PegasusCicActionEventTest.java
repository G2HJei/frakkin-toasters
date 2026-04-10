package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PEGASUS_CIC;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;

class PegasusCicActionEventTest {
    Game game = new Game(KOBOL, 3);

    @Test
    void shouldDamagePegasus() {
        new PegasusCicActionEvent().execute(game);
        assertTrue(game.boards().pegasus().damagedLocations().contains(PEGASUS_CIC));
    }
}