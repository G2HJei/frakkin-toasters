package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.*;

class DiscardDownTo1MutinyCardEventTest {

    @Test
    void shouldDiscardAllButSelectedCard() {
        val game = new Game(ObjectiveCard.KOBOL, 3);
        val player = game.player(1);
        player.mutinyCards().add(PANIC, ASSUME_COMMAND, FEED_THE_PEOPLE);

        new DiscardDownTo1MutinyCardEvent(1, PANIC).execute(game);

        assertEquals(List.of(PANIC), player.mutinyCards().cards());
        assertEquals(List.of(ASSUME_COMMAND, FEED_THE_PEOPLE), player.mutinyCards().discardedCards());
    }
}