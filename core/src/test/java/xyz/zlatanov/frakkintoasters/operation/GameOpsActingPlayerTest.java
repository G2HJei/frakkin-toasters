package xyz.zlatanov.frakkintoasters.operation;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameOpsActingPlayerTest extends TestHarness {

    @Test
    void shouldActAsCurrentPlayerByDefault() {
        game.currentPlayerNumber(2);

        assertEquals(2, game.actingPlayerNumber());
        assertEquals(2, game.actingPlayer().number());
    }

    @Test
    void shouldLetAnotherPlayerAct() {
        game.currentPlayerNumber(2);

        ops.actingPlayer(3);

        assertEquals(3, game.actingPlayerNumber());
        assertEquals(3, game.actingPlayer().number());
    }

    @Test
    void shouldKeepCurrentPlayerWhenAnotherPlayerActs() {
        game.currentPlayerNumber(2);

        ops.actingPlayer(3);

        assertEquals(2, game.currentPlayerNumber());
    }

    @Test
    void shouldGoBackToCurrentPlayerWhenActingPlayerIsReset() {
        game.currentPlayerNumber(2);
        ops.actingPlayer(3);

        ops.resetActingPlayer();

        assertEquals(2, game.actingPlayerNumber());
    }

    @Test
    void shouldFollowTheCurrentPlayerWhenTheCurrentPlayerActs() {
        game.currentPlayerNumber(2);
        ops.actingPlayer(2);

        game.currentPlayerNumber(3);

        assertEquals(3, game.actingPlayerNumber());
    }
}
