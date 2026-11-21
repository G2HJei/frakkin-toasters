package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CultLeaderEventProcessorTest extends EventTestHarness<CultLeaderEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(GAIUS_BALTAR_ALT);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, GAIUS_BALTAR_ALT);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    @Test
    void givesAMiracleTokenToAPlayerAtHisLocation() {
        ops.player(2).loseMiracleTokens();

        execute(new CultLeaderEvent(1, 2, true));

        assertEquals(0, player(1).miracleTokens());
        assertEquals(1, player(2).miracleTokens());
    }

    @Test
    void takesTheirMiracleToken() {
        execute(new CultLeaderEvent(1, 2, false));

        assertEquals(2, player(1).miracleTokens());
        assertEquals(0, player(2).miracleTokens());
    }

    @Test
    void notToAPlayerWhoHasOneAlready() {
        assertInvalid(new CultLeaderEvent(1, 2, true));
    }

    @Test
    void notAtAnotherLocation() {
        ops.player(3).loseMiracleTokens();

        assertInvalid(new CultLeaderEvent(1, 3, true));
    }
}
