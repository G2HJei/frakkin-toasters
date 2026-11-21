package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ARREST_ORDER;

class DiscardPlayedQuorumCardEventProcessorTest extends EventTestHarness<DiscardPlayedQuorumCardEvent> {

    @Test
    void theCardGoesToTheDiscardPile() {
        execute(new DiscardPlayedQuorumCardEvent(ARREST_ORDER));

        assertEquals(List.of(ARREST_ORDER), quorumDeck.discardedCards());
    }
}
