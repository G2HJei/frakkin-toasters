package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.AGGRESSIVE_TACTICS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAVIL;

class AggressiveTacticsEventProcessorTest extends EventTestHarness<AggressiveTacticsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(CAVIL);
        moveTo(CYLON_FLEET, CAVIL);
    }

    // Two actions now, instead of moving, for his miracle token.
    @Test
    void grantsTwoActions() {
        execute(new AggressiveTacticsEvent(1));

        assertEquals(0, player(1).miracleTokens());
        assertFollowup(all(new GrantEvent(1, ACTION, AGGRESSIVE_TACTICS), new GrantEvent(1, ACTION, AGGRESSIVE_TACTICS)));
    }

    @Test
    void notWithoutAMiracleToken() {
        player(1).exhaustMiracleToken();

        assertInvalid(new AggressiveTacticsEvent(1));
    }
}
