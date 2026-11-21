package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.PROBATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class ProbationEventProcessorTest extends EventTestHarness<ProbationEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(PROBATION);
    }

    @Test
    void aPlayerIsOnProbation() {
        execute(new ProbationEvent(1, 2));

        assertEquals(Optional.of(2), game.quorumCardHolder(PROBATION));
    }

    // The card gives it to "any player".
    @Test
    void thePresidentToo() {
        execute(new ProbationEvent(1, 1));

        assertEquals(Optional.of(1), game.quorumCardHolder(PROBATION));
    }

    @Test
    void notSomeoneWhoIsNoPlayer() {
        assertInvalid(new ProbationEvent(1, 7));
    }
}
