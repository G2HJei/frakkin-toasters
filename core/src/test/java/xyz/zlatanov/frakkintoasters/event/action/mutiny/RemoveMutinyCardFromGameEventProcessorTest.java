package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.FEED_THE_PEOPLE;

class RemoveMutinyCardFromGameEventProcessorTest extends EventTestHarness<RemoveMutinyCardFromGameEvent> {

    @Test
    void theCardLeavesTheGame() {
        execute(new RemoveMutinyCardFromGameEvent(FEED_THE_PEOPLE));

        assertEquals(List.of(FEED_THE_PEOPLE), game.removedComponents().items());
        assertEquals(List.of(), mutinyDeck.discardedCards());
    }
}
