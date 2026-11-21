package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.METICULOUS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.AARON_DORAL;

class MeticulousEventProcessorTest extends EventTestHarness<MeticulousEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(AARON_DORAL);
        moveTo(HUMAN_FLEET, AARON_DORAL);
        ops.player(1).infiltrateGalactica(COMMAND);
    }

    @Test
    void goesToAnotherCylonLocationAndTakesAnotherAction() {
        execute(new MeticulousEvent(1, CAPRICA));

        assertFalse(player(1).isInfiltrating());
        assertEquals(CAPRICA, locate(AARON_DORAL));
        assertEquals(0, player(1).miracleTokens());
        assertFollowup(new GrantEvent(1, ACTION, METICULOUS));
    }

    @Test
    void theResurrectionShipAsUsual() {
        execute(new MeticulousEvent(1, RESURRECTION_SHIP));

        assertEquals(RESURRECTION_SHIP, locate(AARON_DORAL));
        assertEquals(1, player(1).miracleTokens());
    }

    @Test
    void notWithoutAMiracleToken() {
        ops.player(1).loseMiracleTokens();

        assertInvalid(new MeticulousEvent(1, CAPRICA));
    }

    @Test
    void notAHumanLocation() {
        assertInvalid(new MeticulousEvent(1, COMMAND));
    }
}
