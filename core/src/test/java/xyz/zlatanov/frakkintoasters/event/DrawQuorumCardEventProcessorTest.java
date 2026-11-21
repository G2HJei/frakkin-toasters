package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ACCEPT_PROPHECY;

class DrawQuorumCardEventProcessorTest extends EventTestHarness<DrawQuorumCardEvent> {

    @Test
    void shouldDrawACardForThePresident() {
        quorumDeck.nextCard(ACCEPT_PROPHECY);

        execute(new DrawQuorumCardEvent());

        assertEquals(List.of(ACCEPT_PROPHECY), presidentHand.cards());
    }

    // Every card is in the President's hand, in play or out of the game.
    @Test
    void nothingToDraw() {
        quorumDeck.clear();

        execute(new DrawQuorumCardEvent());

        assertEquals(List.of(), presidentHand.cards());
    }
}
