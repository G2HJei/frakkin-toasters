package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.*;

class DiscardDownTo1MutinyCardEventProcessorTest extends EventTestHarness<DiscardDownTo1MutinyCardEvent> {

    @Test
    void shouldDiscardAllButSelectedCard() {
        player(1).mutinyCards().addOnTop(PANIC, ASSUME_COMMAND, FEED_THE_PEOPLE);

        execute(new DiscardDownTo1MutinyCardEvent(1, PANIC));

        assertEquals(List.of(PANIC), player(1).mutinyCards().cards());
        assertEquals(Set.of(ASSUME_COMMAND, FEED_THE_PEOPLE), new HashSet<>(player(1).mutinyCards().discardedCards()));
    }
}