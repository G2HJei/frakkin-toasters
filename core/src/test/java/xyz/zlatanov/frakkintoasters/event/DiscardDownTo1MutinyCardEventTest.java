package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.*;

class DiscardDownTo1MutinyCardEventTest extends EventTest {

    @Test
    void shouldDiscardAllButSelectedCard() {
        val player1MutinyCards = player(1).mutinyCards();
        player1MutinyCards.add(PANIC, ASSUME_COMMAND, FEED_THE_PEOPLE);

        execute(new DiscardDownTo1MutinyCardEvent(1, PANIC));

        assertEquals(List.of(PANIC), player1MutinyCards.cards());
        assertEquals(List.of(ASSUME_COMMAND, FEED_THE_PEOPLE), player1MutinyCards.discardedCards());
    }
}