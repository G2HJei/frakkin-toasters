package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ACCEPT_PROPHECY;

// "The President discards 2 random Quorum Cards and 2 random Skill Cards." The Skill Cards go by a separate event.
class QuorumInUproarFailEventProcessorTest extends EventTestHarness<QuorumInUproarFailEvent> {

    @Test
    void shouldDiscardTwoQuorumCardsOfThePresident() {
        presidentHand.addOnTop(ACCEPT_PROPHECY, ACCEPT_PROPHECY, ACCEPT_PROPHECY);

        execute(new QuorumInUproarFailEvent());

        assertEquals(1, presidentHand.size());
        assertEquals(2, quorumDeck.discardSize());
    }

    @Test
    void shouldDiscardAllWhenThePresidentHasFewer() {
        presidentHand.addOnTop(ACCEPT_PROPHECY);

        execute(new QuorumInUproarFailEvent());

        assertEquals(0, presidentHand.size());
    }

    @Test
    void shouldDoNothingWithoutQuorumCards() {
        execute(new QuorumInUproarFailEvent());

        assertEquals(0, quorumDeck.discardSize());
    }
}
