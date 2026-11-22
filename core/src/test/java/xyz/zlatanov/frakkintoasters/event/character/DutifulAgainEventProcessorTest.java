package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.EndRemoteActionEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.CommunicationsEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUIS_HOSHI;

class DutifulAgainEventProcessorTest extends EventTestHarness<DutifulAgainEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LOUIS_HOSHI);
    }

    @Test
    void hoshiActivatesTheLocationAgain() {
        moveTo(COMMUNICATIONS, LOUIS_HOSHI);

        execute(new DutifulAgainEvent(1, COMMUNICATIONS));

        assertFollowup(new PlayerDecisionEvent<>(1, CommunicationsEvent.class));
    }

    // From elsewhere, e.g. after an "Unorthodox Plan", as a remote action.
    @Test
    void hoshiActivatesTheLocationAgainFromElsewhere() {
        moveTo(RESEARCH_LAB, LOUIS_HOSHI);

        execute(new DutifulAgainEvent(1, COMMUNICATIONS));

        assertEquals(Optional.of(COMMUNICATIONS), game.turn().remoteActionLocation(1));
        assertFollowup(all(new PlayerDecisionEvent<>(1, LocationEvent.class), new EndRemoteActionEvent(1)));
    }

    // A "Bait" he discarded for it drew him a second Mutiny Card, which sent him to the "Brig" (F-70a615).
    @Test
    void notFromTheBrig() {
        moveTo(BRIG, LOUIS_HOSHI);

        execute(new DutifulAgainEvent(1, COMMUNICATIONS));
    }

    @Test
    void notAnotherLocation() {
        moveTo(RESEARCH_LAB, LOUIS_HOSHI);

        assertInvalid(new DutifulAgainEvent(1, RESEARCH_LAB));
    }
}
