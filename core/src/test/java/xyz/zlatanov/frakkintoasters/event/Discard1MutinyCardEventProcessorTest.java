package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;

class Discard1MutinyCardEventProcessorTest extends EventTestHarness<Discard1MutinyCardEvent> {

    @Test
    void shouldDiscardSelectedCard() {
        mutinyCards(1, PANIC);

        executeAndAssertNoFollowup(new Discard1MutinyCardEvent(1, PANIC));

        assertTrue(player(1).mutinyCards().isEmpty());
        assertEquals(PANIC, player(1).mutinyCards().lastDiscarded());
    }
}