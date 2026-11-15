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
}
