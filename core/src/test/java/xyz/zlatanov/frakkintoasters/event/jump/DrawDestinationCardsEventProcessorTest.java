package xyz.zlatanov.frakkintoasters.event.jump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.DEEP_SPACE;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.ICY_MOON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class DrawDestinationCardsEventProcessorTest extends EventTestHarness<DrawDestinationCardsEvent> {

    @BeforeEach
    void anAdmiral() {
        player(2).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    // "The Admiral draws two cards from the Destination deck, chooses one, and places the other on the bottom of the deck."
    @Test
    void theAdmiralChoosesOneOfTwoCards() {
        destinationDeck.nextCard(ICY_MOON, DEEP_SPACE);

        execute(new DrawDestinationCardsEvent());

        assertFollowup(one(new ChooseDestinationEvent(2, ICY_MOON, DEEP_SPACE),
                new ChooseDestinationEvent(2, DEEP_SPACE, ICY_MOON)));
    }
}
