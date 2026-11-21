package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR_ALT;

class BroadcastEventProcessorTest extends EventTestHarness<BroadcastEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(GAIUS_BALTAR_ALT);
        moveTo(ADMIRALS_QUARTERS, GAIUS_BALTAR_ALT);
    }

    @Test
    void spendsThreeMiracleTokensForTwoOfAResource() {
        ops.player(1).gainMiracleToken();
        ops.player(1).gainMiracleToken();

        execute(new BroadcastEvent(1, FOOD));

        assertEquals(0, player(1).miracleTokens());
        assertFollowup(new GainResourceEvent(FOOD, 2));
    }

    @Test
    void notWithFewerThanThreeMiracleTokens() {
        assertInvalid(new BroadcastEvent(1, FOOD));
    }
}
