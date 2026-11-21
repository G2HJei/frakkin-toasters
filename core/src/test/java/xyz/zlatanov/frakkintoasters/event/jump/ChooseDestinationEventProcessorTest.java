package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.CYLON_AMBUSH;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.DEEP_SPACE;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.ICY_MOON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class ChooseDestinationEventProcessorTest extends EventTestHarness<ChooseDestinationEvent> {

    @BeforeEach
    void anAdmiral() {
        player(2).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    @Test
    void theOtherCardGoesToTheBottomOfTheDeck() {
        destinationDeck.nextCard(ICY_MOON, DEEP_SPACE);
        val drawn = ops.galactica().drawDestinationCards(2);

        execute(new ChooseDestinationEvent(2, drawn.get(0), List.of(drawn.get(1))));

        assertEquals(drawn.get(1), game.decks().destination().cards().getLast());
        assertFollowup(new ResolveDestinationCardEvent(drawn.get(0)));
    }

    // The Mission Specialist's two other cards go to the bottom in the order drawn.
    @Test
    void theOtherCardsGoToTheBottomInOrder() {
        destinationDeck.nextCard(ICY_MOON, DEEP_SPACE, CYLON_AMBUSH);
        val drawn = ops.galactica().drawDestinationCards(3);

        execute(new ChooseDestinationEvent(2, drawn.get(1), List.of(drawn.get(0), drawn.get(2))));

        val deck = game.decks().destination().cards();
        assertEquals(List.of(drawn.get(0), drawn.get(2)), deck.subList(deck.size() - 2, deck.size()));
        assertFollowup(new ResolveDestinationCardEvent(drawn.get(1)));
    }

    @Test
    void theCardsAreDifferent() {
        assertInvalid(new ChooseDestinationEvent(2, ICY_MOON, List.of(ICY_MOON)));
    }

    @Test
    void notWithoutOtherCards() {
        assertInvalid(new ChooseDestinationEvent(2, ICY_MOON, List.of()));
    }
}
