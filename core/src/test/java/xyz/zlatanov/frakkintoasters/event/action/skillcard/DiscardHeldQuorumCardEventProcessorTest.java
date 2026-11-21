package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.EULOGY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK;

class DiscardHeldQuorumCardEventProcessorTest extends EventTestHarness<DiscardHeldQuorumCardEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TOM_ZAREK);
    }

    @Test
    void theHeldCardGoesToTheDiscardPile() {
        ops.player(1).holdQuorumCard(EULOGY);

        execute(new DiscardHeldQuorumCardEvent(1, EULOGY));

        assertEquals(List.of(), player(1).quorumCards().cards());
        assertEquals(List.of(EULOGY), quorumDeck.discardedCards());
    }

    @Test
    void notACardHeNeverHeld() {
        assertInvalid(new DiscardHeldQuorumCardEvent(1, EULOGY));
    }
}
