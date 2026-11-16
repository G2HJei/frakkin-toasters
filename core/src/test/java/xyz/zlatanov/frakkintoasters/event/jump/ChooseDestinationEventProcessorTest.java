package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        execute(new ChooseDestinationEvent(2, drawn.get(0), drawn.get(1)));

        assertEquals(drawn.get(1), game.decks().destination().cards().getLast());
        assertFollowup(new ResolveDestinationCardEvent(drawn.get(0)));
    }

    @Test
    void onlyTheAdmiralChooses() {
        assertInvalid(new ChooseDestinationEvent(1, ICY_MOON, DEEP_SPACE));
    }

    @Test
    void theTwoCardsAreDifferent() {
        assertInvalid(new ChooseDestinationEvent(2, ICY_MOON, ICY_MOON));
    }
}
