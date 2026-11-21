package xyz.zlatanov.frakkintoasters.event.sandbox;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;

class GiveMutinyCardToPlayerEventProcessorTest extends EventTestHarness<GiveMutinyCardToPlayerEvent> {

    @Test
    void theCardComesOutOfTheDrawPile() {
        mutinyDeck.clear();
        mutinyDeck.addOnTop(PANIC);

        execute(new GiveMutinyCardToPlayerEvent(1, PANIC));

        assertEquals(List.of(PANIC), player(1).mutinyCards().cards());
        assertFalse(mutinyDeck.cards().contains(PANIC));
    }

    @Test
    void notACardThatIsNotInTheDrawPile() {
        mutinyDeck.clear();

        assertInvalid(new GiveMutinyCardToPlayerEvent(1, PANIC));
    }
}
