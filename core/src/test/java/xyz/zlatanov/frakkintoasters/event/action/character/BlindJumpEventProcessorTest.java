package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;

class BlindJumpEventProcessorTest extends EventTestHarness<BlindJumpEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(HELENA_CAIN);
        moveTo(COMMAND, HELENA_CAIN);
    }

    @Test
    void destroysTwoCivilianShipsAndJumps() {
        execute(new BlindJumpEvent(1));

        assertFollowup(all(new DrawAndDestroyCivilianShipsEvent(2), new JumpingTheFleetEvent(true)));
    }

    @Test
    void notAfterSixDistance() {
        ops.galactica().reachDestination(ASTEROID_FIELD);
        ops.galactica().reachDestination(CYLON_AMBUSH);
        ops.galactica().reachDestination(ICY_MOON);

        assertInvalid(new BlindJumpEvent(1));
    }
}
