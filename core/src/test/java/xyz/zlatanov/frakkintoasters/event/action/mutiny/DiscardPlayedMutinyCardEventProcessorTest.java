package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;

class DiscardPlayedMutinyCardEventProcessorTest extends EventTestHarness<DiscardPlayedMutinyCardEvent> {

    @Test
    void theCardGoesToTheDiscardPile() {
        execute(new DiscardPlayedMutinyCardEvent(PANIC));

        assertEquals(List.of(PANIC), mutinyDeck.discardedCards());
    }
}
