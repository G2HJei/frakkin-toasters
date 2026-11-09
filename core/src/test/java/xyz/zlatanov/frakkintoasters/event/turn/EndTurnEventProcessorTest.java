package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndTurnEventProcessorTest extends EventTestHarness<EndTurnEvent> {

    // "he hands the current player token clockwise to the player on his left" (rule 8.1)
    @ParameterizedTest
    @CsvSource({"1, 2", "2, 3", "3, 1"})
    void theNextPlayerStartsTheirTurn(int current, int next) {
        game.turn().currentPlayerNumber(current);

        execute(new EndTurnEvent());

        assertEquals(next, game.turn().currentPlayerNumber());
        assertFollowup(new StartTurnEvent());
    }

    // "If at least one resource ... is depleted to 0 at the end of a player's turn, the game immediately ends and the
    // Cylons win." (rule 16.1)
    @ParameterizedTest
    @EnumSource(Resource.class)
    void theCylonsWinWhenAResourceIsAtZero(Resource resource) {
        ops.galactica().decreaseResource(resource, 20);

        execute(new EndTurnEvent());

        assertEquals(1, game.turn().currentPlayerNumber());
        assertFollowup(new CylonsWinEvent());
    }

    // "Note that it is possible for the humans to reach 0 of a resource type and then increase it during the same turn
    // without losing the game."
    @ParameterizedTest
    @EnumSource(Resource.class)
    void aResourceThatRecoveredBeforeTheEndOfTheTurnIsFine(Resource resource) {
        ops.galactica().decreaseResource(resource, 20); // a resource doesn't go below 0
        ops.galactica().increaseResource(resource, 1);

        execute(new EndTurnEvent());

        assertFollowup(new StartTurnEvent());
    }
}
