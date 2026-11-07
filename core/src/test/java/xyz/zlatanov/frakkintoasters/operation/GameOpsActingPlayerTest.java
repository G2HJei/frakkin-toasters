package xyz.zlatanov.frakkintoasters.operation;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameOpsActingPlayerTest extends TestHarness {

    @Test
    void shouldActAsCurrentPlayerByDefault() {
        game.turn().currentPlayerNumber(2);

        assertEquals(2, game.turn().actingPlayerNumber());
        assertEquals(2, game.actingPlayer().number());
    }

    @Test
    void shouldLetAnotherPlayerAct() {
        game.turn().currentPlayerNumber(2);

        ops.actingPlayer(3);

        assertEquals(3, game.turn().actingPlayerNumber());
        assertEquals(3, game.actingPlayer().number());
    }

    @Test
    void shouldKeepCurrentPlayerWhenAnotherPlayerActs() {
        game.turn().currentPlayerNumber(2);

        ops.actingPlayer(3);

        assertEquals(2, game.turn().currentPlayerNumber());
    }

    @Test
    void shouldGoBackToCurrentPlayerWhenActingPlayerIsReset() {
        game.turn().currentPlayerNumber(2);
        ops.actingPlayer(3);

        ops.resetActingPlayer();

        assertEquals(2, game.turn().actingPlayerNumber());
    }

    @Test
    void shouldFollowTheCurrentPlayerWhenTheCurrentPlayerActs() {
        game.turn().currentPlayerNumber(2);
        ops.actingPlayer(2);

        game.turn().currentPlayerNumber(3);

        assertEquals(3, game.turn().actingPlayerNumber());
    }
}
